package org.elpis.reactive.websockets.config.annotation.impl;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.annotation.SocketApiAnnotationEvaluator;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.request.SocketMessageBody;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;

/**
 * Implementation of {@link SocketApiAnnotationEvaluator} based on {@link SocketMessageBody @SocketMessageBody}.
 *
 * @author Alex Zharkov
 * @see SocketApiAnnotationEvaluator
 * @see SocketMessageBody
 * @since 0.1.0
 */
@Component
public class MessageBodyAnnotationEvaluator implements SocketApiAnnotationEvaluator<SocketMessageBody> {

    /**
     * See {@link SocketApiAnnotationEvaluator#evaluate(WebSocketSessionContext, Parameter, Annotation)}
     *
     * @since 0.1.0
     */
    @Override
    public Object evaluate(@NonNull final WebSocketSessionContext context, @NonNull final Parameter parameter,
                           @NonNull final SocketMessageBody annotation) {

        final Class<?> parameterType = parameter.getType();
        if (!context.isInbound()) {
            throw new WebSocketConfigurationException("Unable register outbound method `@Outbound %s()` since " +
                    "it cannot accept Flux<WebSocketMessage> or Mono<WebSocketMessage>", parameter.getDeclaringExecutable().getName());
        }

        final boolean isFlux = Flux.class.isAssignableFrom(parameterType);
        final boolean isMono = Mono.class.isAssignableFrom(parameterType);

        if (!isFlux && !isMono) {
            throw new WebSocketConfigurationException("Unable register outbound method `@Inbound %s()` since " +
                            "it should accept Flux<WebSocketMessage> or Mono<WebSocketMessage> instance, but `%s` was found instead",
                            parameter.getDeclaringExecutable().getName(), parameterType.getSimpleName());
        }

        final ParameterizedType parameterizedType = TypeUtils.cast(parameter.getParameterizedType());
        final Class<?> persistentClass = TypeUtils.cast(parameterizedType.getActualTypeArguments()[0]);

        if (!WebSocketMessage.class.isAssignableFrom(persistentClass)) {
            throw new WebSocketConfigurationException("Unable register outbound method `@Inbound %s()` since " +
                            "it should accept Flux<WebSocketMessage> or Mono<WebSocketMessage> instance, but `%s<%s>` was found instead",
                    parameter.getDeclaringExecutable().getName(), parameterType.getSimpleName(), persistentClass.getSimpleName());
        }

        final Flux<WebSocketMessage> messageFlux = context.getMessageStream().get();
        return isMono ? messageFlux.next() : messageFlux;
    }

    /**
     * See {@link SocketApiAnnotationEvaluator#getAnnotationType()}
     *
     * @since 0.1.0
     */
    @Override
    public Class<SocketMessageBody> getAnnotationType() {
        return SocketMessageBody.class;
    }
}
