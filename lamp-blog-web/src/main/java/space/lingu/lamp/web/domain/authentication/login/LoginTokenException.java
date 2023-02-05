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

import space.lingu.lamp.ErrorCode;
import space.lingu.lamp.ErrorCodeFinder;
import space.lingu.lamp.BusinessRuntimeException;

/**
 * @author RollW
 */
public class LoginTokenException extends BusinessRuntimeException {
    public LoginTokenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LoginTokenException(ErrorCode errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public LoginTokenException(ErrorCode errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }

    public LoginTokenException(Throwable cause) {
        super(cause);
    }

    public LoginTokenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public LoginTokenException(ErrorCodeFinder errorCodeFinder, Throwable cause) {
        super(errorCodeFinder, cause);
    }

    public LoginTokenException(ErrorCodeFinder codeFinderChain, Throwable cause, String message, Object... args) {
        super(codeFinderChain, cause, message, args);
    }
}