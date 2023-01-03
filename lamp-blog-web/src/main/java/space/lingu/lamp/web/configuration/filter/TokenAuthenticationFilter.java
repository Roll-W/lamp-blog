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

package space.lingu.lamp.web.configuration.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import space.lingu.lamp.web.data.dto.TokenAuthResult;
import space.lingu.lamp.web.service.auth.AuthenticationTokenService;
import space.lingu.lamp.web.service.user.UserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author RollW
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationTokenService authenticationTokenService;
    private final UserDetailsService userDetailsService;

    public TokenAuthenticationFilter(AuthenticationTokenService authenticationTokenService,
                                     UserDetailsService userDetailsService) {
        this.authenticationTokenService = authenticationTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        TokenAuthResult result = authenticationTokenService.verifyToken(token);
        if (!result.state()) {
            filterChain.doFilter(request, response);
            return;
        }
        UserDetails userDetails =
                userDetailsService.loadUserByUserId(result.userId());
        if (userDetails != null) {
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}