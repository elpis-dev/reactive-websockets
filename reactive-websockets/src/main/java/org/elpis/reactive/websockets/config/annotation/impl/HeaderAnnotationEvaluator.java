package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exception.ValidationException;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketHeader;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ValueConstants;

import java.util.List;
import java.util.Optional;

@Component
public class HeaderAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketHeader> {

    @Override
    public Object evaluate(@NonNull WebSocketSessionContext webSocketSessionContext,
                           @NonNull Class<?> parameterType,
                           @NonNull String methodName,
                           @NonNull SocketHeader annotation) {

        final HttpHeaders headers = webSocketSessionContext.getHeaders();

        final Optional<String> defaultValue = Optional.of(annotation.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && annotation.required();

        final Optional<List<String>> values = Optional.ofNullable(headers.get(annotation.value()))
                .filter(l -> !l.isEmpty());

        if (isRequired && values.isEmpty()) {
            throw new ValidationException(String.format("Request header `@SocketHeader %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", annotation.value(), methodName));
        }

        return values.flatMap(l -> List.class.isAssignableFrom(parameterType)
                ? Optional.of(l)
                : l.stream().findFirst().map(v -> (Object) TypeUtils.convert(v, parameterType)))
                .orElseGet(() -> defaultValue.map(v -> (Object) TypeUtils.convert(v, parameterType))
                        .orElse(TypeUtils.getDefaultValueForType(parameterType)));
    }

    @Override
    public Class<SocketHeader> getAnnotationType() {
        return SocketHeader.class;
    }
}
