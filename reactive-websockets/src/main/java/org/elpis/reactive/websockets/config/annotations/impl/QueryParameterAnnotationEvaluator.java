package org.elpis.reactive.websockets.config.annotations.impl;

import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.annotations.request.SocketQueryParam;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ValueConstants;

import java.util.List;
import java.util.Optional;

@Component
public class QueryParameterAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketQueryParam, MultiValueMap<String, String>> {

    @Override
    public Object evaluate(final MultiValueMap<String, String> data, final Class<?> parameterType,
                           final String methodName, final SocketQueryParam annotation) {

        final Optional<String> defaultValue = Optional.of(annotation.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && annotation.required();

        final Optional<List<String>> values = Optional.ofNullable(data.get(annotation.value()))
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
