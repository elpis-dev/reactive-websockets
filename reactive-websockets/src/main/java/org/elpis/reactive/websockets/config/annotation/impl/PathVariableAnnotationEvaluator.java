package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketPathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link SocketApiAnnotationEvaluator} based on {@link SocketPathVariable @SocketPathVariable}.
 *
 * @author Alex Zharkov
 * @see SocketApiAnnotationEvaluator
 * @see SocketPathVariable
 * @since 0.1.0
 */
@Component
public class PathVariableAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketPathVariable> {

    /**
     * See {@link SocketApiAnnotationEvaluator#evaluate(WebSocketSessionContext, Parameter, Annotation)}
     *
     * @since 0.1.0
     */
    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext context, @NonNull final Parameter parameter,
                           @NonNull final SocketPathVariable annotation) {

        final Class<?> parameterType = parameter.getType();
        final Map<String, String> pathParameters = context.getPathParameters();
        final Optional<String> value = Optional.ofNullable(pathParameters.get(annotation.value()))
                .filter(s -> !s.isEmpty());

        if (annotation.required() && value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Path parameter `@SocketPathVariable %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", annotation.value(), parameter.getDeclaringExecutable().getName()));
        }

        return value.map(v -> (Object) TypeUtils.convert(v, parameterType))
                .orElse(TypeUtils.getDefaultValueForType(parameterType));
    }

    /**
     * See {@link SocketApiAnnotationEvaluator#getAnnotationType()}
     *
     * @since 0.1.0
     */
    @Override
    public Class<SocketPathVariable> getAnnotationType() {
        return SocketPathVariable.class;
    }
}
