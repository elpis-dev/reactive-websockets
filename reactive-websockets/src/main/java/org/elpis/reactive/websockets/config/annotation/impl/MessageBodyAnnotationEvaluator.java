package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exception.ValidationException;
import org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class MessageBodyAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketMessageBody> {

    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext webSocketSessionContext,
                           @NonNull final Class<?> parameterType, @NonNull final String methodName,
                           @NonNull final SocketMessageBody annotation) {

        if (!webSocketSessionContext.isInbound()) {
            throw new ValidationException(String.format("Unable register outbound method `@Outbound %s()` since " +
                    "it cannot accept Flux<WebSocketMessage>", methodName));
        }

        if (!Flux.class.isAssignableFrom(parameterType)) {
            throw new ValidationException(String.format("Unable register outbound method `@Inbound %s()` since " +
                    "it should accept Flux<WebSocketMessage> instance, but `%s` was found instead", methodName, parameterType));
        }

        return webSocketSessionContext.getMessageStream().get();
    }

    @Override
    public Class<SocketMessageBody> getAnnotationType() {
        return SocketMessageBody.class;
    }
}
