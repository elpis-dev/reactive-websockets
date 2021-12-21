package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.security.Principal;

/**
 * Implementation of {@link SocketApiAnnotationEvaluator} based on {@link SocketAuthentication @SocketAuthentication}.
 *
 * @author Alex Zharkov
 * @see SocketApiAnnotationEvaluator
 * @see SocketAuthentication
 * @since 0.1.0
 */
@Component
public class AuthenticationAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketAuthentication> {

    /**
     * See {@link SocketApiAnnotationEvaluator#evaluate(WebSocketSessionContext, Parameter, String, Annotation)}
     *
     * @since 0.1.0
     */
    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext context, @NonNull final Parameter parameter,
                           @NonNull final String methodName, @NonNull final SocketAuthentication annotation) {

        final Class<?> parameterType = parameter.getType();

        final Principal principal = context.getAuthentication();
        final Class<?> principalType = principal.getClass();

        if (Anonymous.class.isAssignableFrom(principalType)) {
            return null;
        } else if (WebSocketPrincipal.class.isAssignableFrom(principalType)) {
            final WebSocketPrincipal<?> webSocketPrincipal = TypeUtils.cast(principal, WebSocketPrincipal.class);

            if (!Principal.class.isAssignableFrom(parameterType) && !parameterType.isAssignableFrom(webSocketPrincipal.getAuthentication().getClass())) {
                throw new WebSocketConfigurationException(String.format("Unable register method `%s()`. Requested @SocketAuthentication type: %s, found: %s",
                        methodName, parameterType.getName(), webSocketPrincipal.getAuthentication().getClass().getName()));
            }

            return Principal.class.isAssignableFrom(parameterType)
                    ? webSocketPrincipal
                    : webSocketPrincipal.getAuthentication();
        } else if (Authentication.class.isAssignableFrom(principalType)) {
            final Authentication authentication = TypeUtils.cast(principal, Authentication.class);

            return Authentication.class.isAssignableFrom(parameterType)
                    ? authentication
                    : this.getPrincipalDetails(authentication, parameterType);
        } else {
            return principal;
        }
    }

    private Object getPrincipalDetails(final Authentication authentication, final Class<?> parameterType) {
        return Principal.class.isAssignableFrom(parameterType) ? authentication.getPrincipal() : authentication.getDetails();
    }

    /**
     * See {@link SocketApiAnnotationEvaluator#getAnnotationType()}
     *
     * @since 0.1.0
     */
    @Override
    public Class<SocketAuthentication> getAnnotationType() {
        return SocketAuthentication.class;
    }
}
