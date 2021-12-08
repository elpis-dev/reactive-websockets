package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exception.ValidationException;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketAuthentication;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AuthenticationAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketAuthentication> {

    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext webSocketSessionContext,
                           @NonNull final Class<?> parameterType,
                           @NonNull final String methodName,
                           @NonNull final SocketAuthentication annotation) {

        final Principal principal = webSocketSessionContext.getAuthentication();
        final Class<?> principalType = principal.getClass();

        if (Anonymous.class.isAssignableFrom(principalType)) {
            return null;
        } else if (WebSocketPrincipal.class.isAssignableFrom(principalType)) {
            final WebSocketPrincipal<?> webSocketPrincipal = TypeUtils.cast(principal, WebSocketPrincipal.class);

            if (!Principal.class.isAssignableFrom(parameterType) && !parameterType.isAssignableFrom(webSocketPrincipal.getAuthentication().getClass())) {
                throw new ValidationException(String.format("Unable register method `%s()`. Requested @SocketAuthentication type: %s, found: %s",
                        methodName, parameterType.getName(), webSocketPrincipal.getAuthentication().getClass().getName()));
            }

            return Principal.class.isAssignableFrom(parameterType)
                    ? webSocketPrincipal
                    : webSocketPrincipal.getAuthentication();
        } else if (Authentication.class.isAssignableFrom(principalType)) {
            final Authentication authentication = TypeUtils.cast(principal, Authentication.class);

            return Authentication.class.isAssignableFrom(parameterType)
                    ? authentication
                    : Principal.class.isAssignableFrom(parameterType)
                        ? authentication.getPrincipal()
                        : authentication.getDetails();
        } else {
            return principal;
        }
    }

    @Override
    public Class<SocketAuthentication> getAnnotationType() {
        return SocketAuthentication.class;
    }
}
