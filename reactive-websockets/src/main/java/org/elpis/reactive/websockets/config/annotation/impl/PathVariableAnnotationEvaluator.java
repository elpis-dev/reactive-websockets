package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exception.ValidationException;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketPathVariable;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

@Component
public class PathVariableAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketPathVariable> {

    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext webSocketSessionContext,
                           @NonNull final Parameter parameter, @NonNull final String methodName,
                           @NonNull final SocketPathVariable annotation) {

        final Class<?> parameterType = parameter.getType();

        final Map<String, String> pathParameters = webSocketSessionContext.getPathParameters();

        final Optional<String> value = Optional.ofNullable(pathParameters.get(annotation.value()))
                .filter(s -> !s.isEmpty());

        if (annotation.required() && value.isEmpty()) {
            throw new ValidationException(String.format("Path parameter `@SocketPathVariable %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", annotation.value(), methodName));
        }

        return value.map(v -> (Object) TypeUtils.convert(v, parameterType))
                .orElse(TypeUtils.getDefaultValueForType(parameterType));
    }

    @Override
    public Class<SocketPathVariable> getAnnotationType() {
        return SocketPathVariable.class;
    }
}
