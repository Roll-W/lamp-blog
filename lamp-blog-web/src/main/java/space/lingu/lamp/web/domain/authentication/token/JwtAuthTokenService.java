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

package space.lingu.lamp.web.domain.authentication.token;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import space.lingu.lamp.web.common.keys.SecurityConfigKeys;
import space.lingu.lamp.web.system.setting.SettingLoader;
import tech.rollw.common.web.AuthErrorCode;
import tech.rollw.common.web.system.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * @author RollW
 */
@Service
public class JwtAuthTokenService implements AuthenticationTokenService {
    private static final String TOKEN_HEAD = "Bearer ";

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenService.class);

    private final SettingLoader settingLoader;

    public JwtAuthTokenService(SettingLoader settingLoader) {
        this.settingLoader = settingLoader;
    }

    @Override
    public String generateAuthToken(long userId, String signature) {
        String timeValue = settingLoader.getSettingValue(
                SecurityConfigKeys.KEY_TOKEN_EXPIRE_TIME, null);
        if (Strings.isNullOrEmpty(timeValue)) {
            return generateAuthToken(userId, signature, DAYS_7);
        }
        try {
            long expireTime = Long.parseLong(timeValue);
            return generateAuthToken(userId, signature, expireTime);
        } catch (NumberFormatException e) {
            logger.error("Invalid token expire time setting: {} and fall back to default," +
                            " please check the setting value of {}.",
                    timeValue, SecurityConfigKeys.KEY_TOKEN_EXPIRE_TIME);
            return generateAuthToken(userId, signature, DAYS_7);
        }
    }

    @Override
    public String generateAuthToken(long userId, String signature,
                                    long expireTimeInSecond) {
        Key key = Keys.hmacShaKeyFor(signature.getBytes(StandardCharsets.UTF_8));
        String rawToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(getExpirationDateFromNow(expireTimeInSecond))
                .setIssuer(settingLoader.getSettingValue(
                        SecurityConfigKeys.KEY_TOKEN_ISSUER,
                        "Lingu Lamp Blog.")
                )
                .signWith(key)
                .compact();
        return TOKEN_HEAD + rawToken;
    }

    @Override
    public TokenAuthResult verifyToken(String token, String signature) {
        if (token == null) {
            throw new AuthenticationException(AuthErrorCode.ERROR_INVALID_TOKEN);
        }
        if (!token.startsWith(TOKEN_HEAD)) {
            throw new AuthenticationException(AuthErrorCode.ERROR_INVALID_TOKEN);
        }
        String rawToken = token.substring(TOKEN_HEAD.length());
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signature.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(rawToken)
                    .getBody();
            long userId = Long.parseLong(claims.getSubject());
            return TokenAuthResult.success(userId, token);
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException(AuthErrorCode.ERROR_TOKEN_EXPIRED);
        } catch (SecurityException e) {
            throw new AuthenticationException(AuthErrorCode.ERROR_INVALID_TOKEN);
        } catch (NumberFormatException e) {
            logger.error("Invalid jwt token number format: {}", rawToken);
            throw new AuthenticationException(AuthErrorCode.ERROR_INVALID_TOKEN);
        } catch (Exception e) {
            throw new AuthenticationException(AuthErrorCode.ERROR_INVALID_TOKEN, e);
        }
    }

    @Override
    public Long getUserId(String token) {
        if (!token.startsWith(TOKEN_HEAD)) {
            return null;
        }
        try {
            String rawToken = token.substring(TOKEN_HEAD.length());
            Claims claims = tryParseClaims(rawToken);
            return Long.parseLong(claims.getSubject());
        } catch (JwtException e) {
            return null;
        }
    }

    @SuppressWarnings("all")
    private static Claims tryParseClaims(String token) {
        // https://github.com/jwtk/jjwt/issues/135
        if (!token.contains(".")) {
            throw new IllegalArgumentException("Invalid token format");
        }
        int i = token.lastIndexOf('.');
        String withoutSignature = token.substring(0, i + 1);
        Jwt<Header, Claims> untrusted = Jwts.parserBuilder()
                .setClock(JwtAuthTokenService::getVerifydate)
                .build()
                .parseClaimsJwt(withoutSignature);
        return untrusted.getBody();
    }

    private static final Date VERIFYDATE = new Date(1);

    private static Date getVerifydate() {
        return VERIFYDATE;
    }

    private Date getExpirationDateFromNow(long expireTimeInSecond) {
        long now = System.currentTimeMillis();
        long exp = now + expireTimeInSecond * 1000;
        return new Date(exp);
    }

    //
    private static final long DAYS_7 = 60 * 60 * 24 * 7L;
    private static final long MINUTES_5 = 60 * 5L;
    private static final long SECONDS_5 = 5L;
}
