package org.elpis.reactive.websockets.config.annotations.impl;

import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.utils.TypeUtils;
import org.elpis.reactive.websockets.web.annotations.request.SocketHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ValueConstants;

import java.util.List;
import java.util.Optional;

@Component
public class HeaderAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketHeader, HttpHeaders> {

    @Override
    public Object evaluate(final HttpHeaders headers, final Class<?> parameterType,
                           final String methodName, final SocketHeader requestHeader) {

        final Optional<String> defaultValue = Optional.of(requestHeader.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && requestHeader.required();

        final Optional<List<String>> values = Optional.ofNullable(headers.get(requestHeader.value()))
                .filter(l -> !l.isEmpty());

        if (isRequired && values.isEmpty()) {
            throw new ValidationException(String.format("Request header `@SocketHeader %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", requestHeader.value(), methodName));
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
