/*
 * Copyright (C) 2023 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.lingu.lamp.web.domain.user.service;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import space.lingu.NonNull;
import space.lingu.lamp.RequestMetadata;
import space.lingu.lamp.web.common.ApiContext;
import space.lingu.lamp.web.common.RequestInfo;
import space.lingu.lamp.web.domain.authentication.login.LoginStrategy;
import space.lingu.lamp.web.domain.authentication.login.LoginStrategyType;
import space.lingu.lamp.web.domain.authentication.login.LoginVerifiableToken;
import space.lingu.lamp.web.domain.user.*;
import space.lingu.lamp.web.domain.user.common.UserViewException;
import space.lingu.lamp.web.domain.user.dto.UserInfoSignature;
import space.lingu.lamp.web.domain.user.event.OnUserLoginEvent;
import space.lingu.lamp.web.domain.user.event.OnUserRegistrationEvent;
import space.lingu.lamp.web.domain.user.repository.RegisterVerificationTokenRepository;
import space.lingu.lamp.web.domain.user.repository.UserRepository;
import tech.rollw.common.web.AuthErrorCode;
import tech.rollw.common.web.ErrorCode;
import tech.rollw.common.web.UserErrorCode;
import tech.rollw.common.web.system.AuthenticationException;
import tech.rollw.common.web.system.ContextThreadAware;

import java.io.IOException;
import java.util.*;

/**
 * @author RollW
 */
@Service
public class LoginRegisterService implements RegisterTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(LoginRegisterService.class);

    private final UserRepository userRepository;
    private final RegisterVerificationTokenRepository registerVerificationTokenRepository;
    private final UserManageService userManageService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;
    private final UserSignatureProvider userSignatureProvider;
    private final ContextThreadAware<ApiContext> apiContextThreadAware;
    private final Map<LoginStrategyType, LoginStrategy> loginStrategyMap =
            new EnumMap<>(LoginStrategyType.class);

    public LoginRegisterService(@NonNull List<LoginStrategy> strategies,
                                UserRepository userRepository,
                                RegisterVerificationTokenRepository registerVerificationTokenRepository,
                                UserManageService userManageService,
                                ApplicationEventPublisher eventPublisher,
                                AuthenticationManager authenticationManager,
                                UserSignatureProvider userSignatureProvider,
                                ContextThreadAware<ApiContext> apiContextThreadAware) {
        this.userRepository = userRepository;
        this.registerVerificationTokenRepository = registerVerificationTokenRepository;
        this.userManageService = userManageService;
        this.eventPublisher = eventPublisher;
        this.authenticationManager = authenticationManager;
        this.userSignatureProvider = userSignatureProvider;
        this.apiContextThreadAware = apiContextThreadAware;
        strategies.forEach(strategy ->
                loginStrategyMap.put(strategy.getStrategyType(), strategy));
    }

    public LoginStrategy getLoginStrategy(LoginStrategyType type) {
        return loginStrategyMap.get(type);
    }

    public void sendToken(long userId, LoginStrategyType type) throws IOException {
        LoginStrategy strategy = getLoginStrategy(type);
        User user = userRepository.getById(userId);
        LoginVerifiableToken token = strategy.createToken(user);
        RequestInfo requestInfo = new RequestInfo(LocaleContextHolder.getLocale(), null);
        strategy.sendToken(token, user, requestInfo);
    }

    public void sendToken(String identity, LoginStrategyType type) throws IOException {
        LoginStrategy strategy = getLoginStrategy(type);
        User user = tryGetUser(identity);
        LoginVerifiableToken token = strategy.createToken(user);
        RequestInfo requestInfo = new RequestInfo(LocaleContextHolder.getLocale(), null);
        strategy.sendToken(token, user, requestInfo);
    }

    private ErrorCode verifyToken(String token,
                                  User user,
                                  LoginStrategyType type) {
        LoginStrategy strategy = getLoginStrategy(type);
        return strategy.verify(token, user);
    }

    private User tryGetUser(String identity) {
        if (identity.contains("@")) {
            return userRepository.getByEmail(identity);
        }
        return userRepository.getByUsername(identity);
    }

    public UserInfoSignature loginUser(String identity,
                                       String token,
                                       RequestMetadata metadata,
                                       LoginStrategyType type) {
        Preconditions.checkNotNull(identity, "identity cannot be null");
        Preconditions.checkNotNull(token, "token cannot be null");

        User user = tryGetUser(identity);
        if (user == null) {
            throw new UserViewException(UserErrorCode.ERROR_USER_NOT_EXIST);
        }
        ErrorCode code = verifyToken(token, user, type);
        if (code.failed()) {
            throw new UserViewException(code);
        }
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
        authentication = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        OnUserLoginEvent onUserLoginEvent = new OnUserLoginEvent(user, metadata);
        eventPublisher.publishEvent(onUserLoginEvent);
        String signature = userSignatureProvider.getSignature(user.getUserId());
        return UserInfoSignature.from(user, signature);
    }

    public AttributedUser registerUser(String username, String password,
                                       String email) {
        boolean hasUsers = userRepository.hasUsers();
        Role role = hasUsers ? Role.USER : Role.ADMIN;
        boolean enabled = !hasUsers;
        AttributedUser user =
                userManageService.createUser(username, password, email, role, enabled);

        if (!enabled) {
            OnUserRegistrationEvent event = new OnUserRegistrationEvent(
                    user, Locale.getDefault(),
                    "http://localhost:5000/user/register/activate/");
            // TODO: get url from config
            eventPublisher.publishEvent(event);
        }

        logger.info("Register username: {}, email: {}, role: {}, id: {}",
                username, email,
                user.getRole(),
                user.getUserId()
        );
        return user;
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public String createRegisterToken(UserIdentity userIdentity) {
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();
        long expiryTime = RegisterVerificationToken.calculateExpiryDate();
        RegisterVerificationToken registerVerificationToken = new RegisterVerificationToken(
                null, token, userIdentity.getUserId(), expiryTime, false
        );
        registerVerificationTokenRepository.insert(registerVerificationToken);
        return uuid.toString();
    }

    @Override
    public void verifyRegisterToken(String token) {
        RegisterVerificationToken verificationToken =
                registerVerificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new AuthenticationException(AuthErrorCode.ERROR_TOKEN_NOT_EXIST);
        }
        if (verificationToken.used()) {
            throw new AuthenticationException(AuthErrorCode.ERROR_TOKEN_USED);
        }
        if (verificationToken.isExpired()) {
            throw new AuthenticationException(AuthErrorCode.ERROR_TOKEN_EXPIRED);
        }
        registerVerificationTokenRepository.makeTokenVerified(verificationToken);
        User user = userRepository
                .getById(verificationToken.userId());
        if (user.isCanceled()) {
            throw new AuthenticationException(UserErrorCode.ERROR_USER_CANCELED);
        }
        if (user.isEnabled()) {
            throw new AuthenticationException(UserErrorCode.ERROR_USER_ALREADY_ACTIVATED);
        }
        userRepository.enableUser(user);
    }

    public void resendToken(String username) {
        User user = userRepository.getByUsername(username);
        if (user == null) {
            throw new UserViewException(UserErrorCode.ERROR_USER_NOT_EXIST);
        }
        ApiContext apiContext = apiContextThreadAware.getContextThread()
                .getContext();
        Locale locale = apiContext == null
                ? Locale.getDefault()
                : apiContext.getLocale();
        OnUserRegistrationEvent event = new OnUserRegistrationEvent(
                user, locale,
                // TODO: get url from config
                "http://localhost:5000/user/register/activate/"
        );
        eventPublisher.publishEvent(event);
    }
}
