package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
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


    protected BroadcastWebSocketResourceHandler(final WebSocketEventManagerFactory eventManagerFactory,
                                                final WebSocketSessionRegistry webSocketSessionRegistry,
                                                final String pathTemplate,
                                                final boolean heartbeatEnabled,
                                                final long heartbeatInterval,
                                                final long heartbeatTimeout) {

        super(eventManagerFactory, webSocketSessionRegistry, pathTemplate, heartbeatEnabled, heartbeatInterval, heartbeatTimeout);
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

        final Flux<WebSocketMessage> serverPings = Flux.interval(Duration.ofSeconds(this.getHeartbeatInterval()))
                .map(aLong -> session.pingMessage(dataBufferFactory -> session.bufferFactory().allocateBuffer(256)));
        final Flux<WebSocketMessage> pongs = this.pongMessages.asFlux();
        if (publisher != null) {
            final Flux<WebSocketMessage> messages = this.mapOutput(session, publisher);
            return this.isHeartbeatEnabled() ? Flux.merge(messages, serverPings, pongs) : messages;
        } else {
            this.run(webSocketSessionContext, socketMessageFlux);
        }

        return this.isHeartbeatEnabled() ? Flux.merge(serverPings, pongs) : null;
    }

}
