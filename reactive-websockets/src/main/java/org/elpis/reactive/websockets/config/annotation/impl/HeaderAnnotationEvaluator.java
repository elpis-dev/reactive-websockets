package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link SocketApiAnnotationEvaluator} based on {@link SocketHeader @SocketHeader}.
 *
 * @author Alex Zharkov
 * @see SocketApiAnnotationEvaluator
 * @see SocketHeader
 * @since 0.1.0
 */
@Component
public class HeaderAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketHeader> {

    /**
     * See {@link SocketApiAnnotationEvaluator#evaluate(WebSocketSessionContext, Parameter, String, Annotation)}
     *
     * @since 0.1.0
     */
    @Override
    public Object evaluate(@NonNull WebSocketSessionContext context, @NonNull Parameter parameter,
                           @NonNull String methodName, @NonNull SocketHeader annotation) {

        final Class<?> parameterType = parameter.getType();
        final HttpHeaders headers = context.getHeaders();

        final Optional<String> defaultValue = Optional.of(annotation.defaultValue())
                .filter(s -> !s.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(s));

        final boolean isRequired = defaultValue.isEmpty() && annotation.required();

        final Optional<List<String>> values = Optional.ofNullable(headers.get(annotation.value()))
                .filter(list -> !list.isEmpty());

        if (isRequired && values.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Request header `@SocketHeader %s` at method `%s()` " +
                    "was marked as `required` but was not found on request", annotation.value(), methodName));
        }

        return values.flatMap(list -> List.class.isAssignableFrom(parameterType)
                        ? Optional.of(list)
                        : list.stream().findFirst().map(v -> (Object) TypeUtils.convert(v, parameterType)))
                .orElseGet(() -> defaultValue.map(v -> (Object) TypeUtils.convert(v, parameterType))
                        .orElse(TypeUtils.getDefaultValueForType(parameterType)));
    }

    /**
     * See {@link SocketApiAnnotationEvaluator#getAnnotationType()}
     *
     * @since 0.1.0
     */
    @Override
    public Class<SocketHeader> getAnnotationType() {
        return SocketHeader.class;
    }
}
