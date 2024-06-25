package org.elpis.reactive.websockets.config.handler;

import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

public abstract class BroadcastWebSocketResourceHandler extends BaseWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Sinks.Many<WebSocketMessage> sink = Sinks.many()
            .multicast()
            .onBackpressureBuffer();

    protected BroadcastWebSocketResourceHandler(final WebSessionRegistry sessionRegistry,
                                                final String pathTemplate,
                                                final boolean pingPongEnabled,
                                                final long pingPongInterval) {

        super(pathTemplate, sessionRegistry, pingPongEnabled, pingPongInterval);
    }

    @Override
    protected Flux<Void> buildChain(WebSocketSession session, WebSocketSessionContext webSocketSessionContext) {
        final Flux<WebSocketMessage> socketMessageFlux = this.sink.asFlux().share();

        final Flux<WebSocketMessage> messages = this.getMessages(session, webSocketSessionContext, socketMessageFlux);

        final Mono<Void> input = session.receive()
                .doOnNext(webSocketMessage -> {
                    final WebSocketMessage.Type type = webSocketMessage.getType();
                    if (this.isPingPongEnabled() && type == WebSocketMessage.Type.PING) {
                        this.sink.tryEmitNext(session.pongMessage(dataBuffer ->
                                session.bufferFactory().allocateBuffer(256)));
                    } else if (this.isPingPongEnabled() && type == WebSocketMessage.Type.PONG) {
                        log.info("Got PONG response from client");
                    } else {
                        sink.tryEmitNext(webSocketMessage);
                    }
                }).then();

        return messages != null ? Flux.merge(input, session.send(messages)) : input.flux();
    }

    private Flux<WebSocketMessage> getMessages(final WebSocketSession session,
                                               final WebSocketSessionContext webSocketSessionContext,
                                               final Flux<WebSocketMessage> socketMessageFlux) {

        final Publisher<?> publisher = this.apply(webSocketSessionContext, socketMessageFlux
                .filter(webSocketMessage -> webSocketMessage.getType() == WebSocketMessage.Type.TEXT
                        || webSocketMessage.getType() == WebSocketMessage.Type.BINARY));

        final Flux<WebSocketMessage> serverPings = Flux.interval(Duration.ofMillis(this.getPingInterval()))
                .map(aLong -> session.pingMessage(dataBufferFactory -> session.bufferFactory().allocateBuffer(256)));
        if (publisher != null) {
            final Flux<WebSocketMessage> messages = this.mapOutput(session, publisher);
            return this.isPingPongEnabled() ? Flux.merge(messages, serverPings) : messages;
        } else {
            this.run(webSocketSessionContext, socketMessageFlux);
        }

        return this.isPingPongEnabled() ? serverPings : null;
    }

}
