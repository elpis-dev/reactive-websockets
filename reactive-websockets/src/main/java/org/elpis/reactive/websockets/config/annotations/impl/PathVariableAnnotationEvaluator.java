package org.elpis.reactive.websockets.config.annotations.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.annotations.request.SocketPathVariable;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class PathVariableAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketPathVariable> {

    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext webSocketSessionContext,
                           @NonNull final Class<?> parameterType, @NonNull final String methodName,
                           @NonNull final SocketPathVariable annotation) {

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
