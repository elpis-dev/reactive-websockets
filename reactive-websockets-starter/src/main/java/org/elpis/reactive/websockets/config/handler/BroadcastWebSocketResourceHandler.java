package org.elpis.reactive.websockets.config.handler;

import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

public abstract class BroadcastWebSocketResourceHandler extends BaseWebSocketHandler {

    private final Sinks.Many<WebSocketMessage> sink = Sinks.many()
            .multicast()
            .onBackpressureBuffer();

    private final Sinks.Many<WebSocketMessage> pongMessages = Sinks.many()
            .multicast()
            .onBackpressureBuffer();


    protected BroadcastWebSocketResourceHandler(final WebSessionRegistry sessionRegistry,
                                                final String pathTemplate,
                                                final boolean pingEnabled,
                                                final long pingInterval) {

        super(pathTemplate, sessionRegistry, pingEnabled, pingInterval);
    }

    @Override
    protected Flux<Void> buildChain(WebSocketSession session, WebSocketSessionContext webSocketSessionContext) {
        final Flux<WebSocketMessage> socketMessageFlux = this.sink.asFlux().share();

        final Flux<WebSocketMessage> messages = this.getMessages(session, webSocketSessionContext, socketMessageFlux);

        final Mono<Void> input = session.receive()
                .filter(webSocketMessage -> webSocketMessage.getType() != WebSocketMessage.Type.PING
                        && webSocketMessage.getType() != WebSocketMessage.Type.PONG)
                .doOnNext(sink::tryEmitNext).then();

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
        final Flux<WebSocketMessage> pongs = this.pongMessages.asFlux();
        if (publisher != null) {
            final Flux<WebSocketMessage> messages = this.mapOutput(session, publisher);
            return this.isPingEnabled() ? Flux.merge(messages, serverPings, pongs) : messages;
        } else {
            this.run(webSocketSessionContext, socketMessageFlux);
        }

        return this.isPingEnabled() ? Flux.merge(serverPings, pongs) : null;
    }

}
