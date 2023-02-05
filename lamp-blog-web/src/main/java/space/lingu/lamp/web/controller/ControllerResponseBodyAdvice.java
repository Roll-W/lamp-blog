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

package space.lingu.lamp.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import space.lingu.NonNull;
import space.lingu.lamp.ErrorCodeMessageProvider;
import space.lingu.lamp.HttpResponseBody;
import space.lingu.lamp.HttpResponseEntity;

import java.util.Objects;

/**
 * @author RollW
 */
@ControllerAdvice
public class ControllerResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    private final ErrorCodeMessageProvider errorCodeMessageProvider;
    private final MessageSource messageSource;
    private static final Logger logger = LoggerFactory.getLogger(ControllerResponseBodyAdvice.class);

    public ControllerResponseBodyAdvice(ErrorCodeMessageProvider errorCodeMessageProvider,
                                        MessageSource messageSource) {
        this.errorCodeMessageProvider = errorCodeMessageProvider;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return checkIfJsonConverter(converterType);
    }

    private boolean checkIfJsonConverter(Class<? extends HttpMessageConverter<?>> converterType) {
        // or other json converter
        return converterType.equals(MappingJackson2HttpMessageConverter.class);
    }

    @Override
    public Object beforeBodyWrite(
            Object obj,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof HttpResponseEntity<?>) {
            return obj;
        }
        if (!(obj instanceof HttpResponseBody<?> body)) {
            return obj;
        }
        HttpMethod method = request.getMethod();
        String rawTip = tryGetRawTip(body);
        String newTip = replaceTipIfNecessary(body);
        int rawCode = body.getStatus();
        int newCode = tryReplaceStatusCode(body, method);
        if (Objects.equals(rawTip, newTip) && rawCode == newCode) {
            return body;
        }
        response.setStatusCode(HttpStatus.valueOf(newCode));
        return body.fork(newTip, newCode);
    }

    private int tryReplaceStatusCode(HttpResponseBody<?> body, HttpMethod method) {
        int code = body.getStatus();
        if (code != 200) {
            return code;
        }
        return switch (method) {
            case POST, PUT, PATCH -> 201;
            case DELETE -> 204;
            default -> 200;
        };
    }

    private String tryGetRawTip(HttpResponseBody<?> responseBody) {
        if (responseBody == null) {
            return null;
        }
        return responseBody.rawTip();
    }

    private String replaceTipIfNecessary(HttpResponseBody<?> responseBody) {
        if (responseBody == null) {
            return null;
        }
        String message = responseBody.rawTip();
        if (message != null) {
            return tryMessageSource(message);
        }
        return errorCodeMessageProvider.getMessage(
                responseBody.getErrorCode(),
                LocaleContextHolder.getLocale()
        );
    }

    private String tryMessageSource(String probablyKey) {
        try {
            return messageSource.getMessage(probablyKey, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return probablyKey;
        }
    }
}