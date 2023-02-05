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

package space.lingu.lamp.web.domain.authentication.login;


import space.lingu.NonNull;
import space.lingu.Nullable;
import space.lingu.lamp.ErrorCode;
import space.lingu.lamp.web.common.RequestInfo;
import space.lingu.lamp.web.domain.user.User;

import java.io.IOException;

/**
 * @author RollW
 */
public interface LoginStrategy {
    LoginVerifiableToken createToken(User user) throws LoginTokenException;

    @NonNull
    ErrorCode verify(String token, @NonNull User user);

    /**
     * Send login token to user.
     *
     * @throws LoginTokenException if login token invalid.
     * @throws IOException         if send failed.
     */
    void sendToken(LoginVerifiableToken token, User user, @Nullable RequestInfo requestInfo)
            throws LoginTokenException, IOException;

    LoginStrategyType getStrategyType();
}