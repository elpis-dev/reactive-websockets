package org.elpis.reactive.websockets.config.annotations.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.annotations.request.SocketQueryParam;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ValueConstants;

import java.util.List;
import java.util.Optional;

@Component
public class QueryParameterAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketQueryParam> {

    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext webSocketSessionContext,
                           @NonNull final Class<?> parameterType, @NonNull final String methodName,
                           @NonNull final SocketQueryParam annotation) {

        final MultiValueMap<String, String> queryParameters = webSocketSessionContext.getQueryParameters();

        final Optional<String> defaultValue = Optional.of(annotation.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && annotation.required();

        final Optional<List<String>> values = Optional.ofNullable(queryParameters.get(annotation.value()))
                .filter(l -> !l.isEmpty());

        if (isRequired && values.isEmpty()) {
            throw new ValidationException(String.format("Request parameter `@SocketQueryParam %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", annotation.value(), methodName));
        }

        return values.flatMap(l -> List.class.isAssignableFrom(parameterType)
                ? Optional.of(l)
                : l.stream().findFirst().map(v -> (Object) TypeUtils.convert(v, parameterType)))
                .orElseGet(() -> defaultValue.map(v -> (Object) TypeUtils.convert(v, parameterType))
                        .orElse(TypeUtils.getDefaultValueForType(parameterType)));
    }

    @Override
    public Class<SocketQueryParam> getAnnotationType() {
        return SocketQueryParam.class;
    }
}
