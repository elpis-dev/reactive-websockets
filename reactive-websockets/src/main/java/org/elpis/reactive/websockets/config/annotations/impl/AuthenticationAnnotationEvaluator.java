package org.elpis.reactive.websockets.config.annotations.impl;

import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.annotations.request.SocketAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class AuthenticationAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketAuthentication, Principal> {

    @Override
    public Object evaluate(final Principal principal, final Class<?> parameterType,
                           final String methodName, final SocketAuthentication annotation) {

        final Class<?> principalType = principal.getClass();

        if (Anonymous.class.isAssignableFrom(principalType)) {
            return null;
        } else if (WebSocketPrincipal.class.isAssignableFrom(principalType)) {
            final WebSocketPrincipal<?> webSocketPrincipal = TypeUtils.cast(principal, WebSocketPrincipal.class);

            if (!parameterType.isAssignableFrom(webSocketPrincipal.getAuthentication().getClass())) {
                throw new ValidationException(String.format("Unable register method `%s()`. Requested @SocketAuthentication type: %s, found: %s",
                        methodName, parameterType.getName(), webSocketPrincipal.getAuthentication().getClass().getName()));
            }

            return webSocketPrincipal.getAuthentication();
        } else if (Authentication.class.isAssignableFrom(principalType)) {
            final Authentication authentication = TypeUtils.cast(principal, Authentication.class);

            return Authentication.class.isAssignableFrom(parameterType)
                    ? authentication
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
