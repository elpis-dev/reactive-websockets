package org.elpis.reactive.websockets.config.annotations.impl;

import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.annotations.request.SocketPathVariable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class PathVariableAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketPathVariable, Map<String, String>> {

    @Override
    public Object evaluate(final Map<String, String> data, final Class<?> parameterType,
                           final String methodName, final SocketPathVariable annotation) {

        final Optional<String> value = Optional.ofNullable(data.get(annotation.value()))
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
