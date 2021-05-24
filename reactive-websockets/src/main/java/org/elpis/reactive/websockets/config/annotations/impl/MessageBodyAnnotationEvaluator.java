package org.elpis.reactive.websockets.config.annotations.impl;

import org.elpis.reactive.websockets.config.annotations.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.exceptions.ValidationException;
import org.elpis.reactive.websockets.web.annotations.request.SocketMessageBody;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;

@Component
public class MessageBodyAnnotationEvaluator extends SocketApiAnnotationEvaluator<SocketMessageBody, WebSocketSession> {

    @Override
    public Object evaluate(final WebSocketSession data, final Class<?> parameterType,
                           final String methodName, final SocketMessageBody annotation) {

        if (!Flux.class.isAssignableFrom(parameterType)) {
            throw new ValidationException(String.format("Unable register outbound method `@Inbound  %s()` since " +
                    "it should accept Flux<WebSocketMessage> instance, but `%s` was found instead", methodName, parameterType));
        }

        return data.receive();
    }

    @Override
    public Class<SocketMessageBody> getAnnotationType() {
        return SocketMessageBody.class;
    }
}
