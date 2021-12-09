package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exception.ValidationException;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;

@Component
public class MessageBodyAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketMessageBody> {

    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext webSocketSessionContext,
                           @NonNull final Parameter parameter, @NonNull final String methodName,
                           @NonNull final SocketMessageBody annotation) {

        final Class<?> parameterType = parameter.getType();

        if (!webSocketSessionContext.isInbound()) {
            throw new ValidationException(String.format("Unable register outbound method `@Outbound %s()` since " +
                    "it doesn't accept Flux<WebSocketMessage> or Mono<WebSocketMessage>", methodName));
        }

        final boolean isFlux = Flux.class.isAssignableFrom(parameterType);
        final boolean isMono = Mono.class.isAssignableFrom(parameterType);

        if (!isFlux && !isMono) {
            throw new ValidationException(String.format("Unable register outbound method `@Inbound %s()` since " +
                    "it should accept Flux<WebSocketMessage> or Mono<WebSocketMessage> instance, but `%s` was found instead", methodName, parameterType.getSimpleName()));
        }

        final ParameterizedType parameterizedType = TypeUtils.cast(parameter.getParameterizedType());
        final Class<?> persistentClass = TypeUtils.cast(parameterizedType.getActualTypeArguments()[0]);

        if (!WebSocketMessage.class.isAssignableFrom(persistentClass)) {
            throw new ValidationException(String.format("Unable register outbound method `@Inbound %s()` since " +
                    "it should accept Flux<WebSocketMessage> or Mono<WebSocketMessage> instance, but `%s<%s>` was found instead", methodName, parameterType.getSimpleName(), persistentClass.getSimpleName()));
        }

        return webSocketSessionContext.getMessageStream().get();
    }

    @Override
    public Class<SocketMessageBody> getAnnotationType() {
        return SocketMessageBody.class;
    }
}
