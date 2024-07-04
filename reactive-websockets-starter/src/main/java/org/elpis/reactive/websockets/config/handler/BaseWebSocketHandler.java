package org.elpis.reactive.websockets.config.handler;

import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.security.Principal;

public abstract class BaseWebSocketHandler implements WebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String pathTemplate;
    private final WebSessionRegistry sessionRegistry;
    private final boolean pingPongEnabled;
    private final long pingInterval;

    protected BaseWebSocketHandler(String pathTemplate,
                                   WebSessionRegistry sessionRegistry,
                                   boolean pingPongEnabled,
                                   long pingInterval) {

        this.pathTemplate = pathTemplate;
        this.sessionRegistry = sessionRegistry;
        this.pingPongEnabled = pingPongEnabled;
        this.pingInterval = pingInterval;
    }

    @Override
    public Mono<Void> handle(final org.springframework.web.reactive.socket.WebSocketSession session) {
        final HandshakeInfo handshakeInfo = session.getHandshakeInfo();

        log.trace("Establishing WebSocketSession: id => {}, uri => {}, address => {}", session.getId(), handshakeInfo.getUri(),
                handshakeInfo.getRemoteAddress());

        final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                .isOpen(session::isOpen)
                .protocol(handshakeInfo.getSubProtocol())
                .id(session.getId())
                .host(handshakeInfo.getUri().getHost())
                .port(handshakeInfo.getUri().getPort())
                .path(pathTemplate)
                .remoteAddress(handshakeInfo.getRemoteAddress())
                .closeStatus(session.closeStatus().share())
                .build();

        this.sessionRegistry.put(session.getId(), webSocketSessionInfo);

        return session.getHandshakeInfo().getPrincipal()
                .switchIfEmpty(Mono.just(new Anonymous()))
                .flatMapMany(principal -> {
                    final WebSocketSessionContext webSocketSessionContext =
                            this.getSessionContext(pathTemplate, session, handshakeInfo, principal);

                    final Flux<Void> closeFlux = this.sessionRegistry.listen()
                            .filter(closeMessage -> closeMessage.getSessionId().equals(session.getId()))
                            .flatMap(closeMessage -> session.close(closeMessage.getCloseStatus()));

                    return Flux.merge(this.buildChain(session, webSocketSessionContext), closeFlux);
                })
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    Flux<WebSocketMessage> mapOutput(final org.springframework.web.reactive.socket.WebSocketSession session, final Publisher<?> publisher) {
        return Flux.from(publisher)
                .flatMap(any -> {
                    if (any instanceof WebSocketMessage webSocketMessage) {
                        return Flux.just(webSocketMessage);
                    } else if (any instanceof byte[] binary) {
                        return Mono.just(session.binaryMessage(factory -> factory.wrap(binary)));
                    } else if (InputStream.class.isAssignableFrom(any.getClass())) {
                        return DataBufferUtils.readByteChannel(() ->
                                                Channels.newChannel((InputStream) any),
                                        DefaultDataBufferFactory.sharedInstance, 4096)
                                .map(dataBuffer -> session.binaryMessage(factory -> dataBuffer));
                    }

                    return JsonMapper.applyWithFlux(any).map(session::textMessage);
                });
    }

    public Publisher<?> apply(final WebSocketSessionContext context, final Flux<WebSocketMessage> messages) {
        //do nothing, waiting for override
        return null;
    }

    public void run(final WebSocketSessionContext context, final Flux<WebSocketMessage> messages) {
        //do nothing, waiting for override
    }

    abstract Flux<Void> buildChain(final org.springframework.web.reactive.socket.WebSocketSession webSocketSession,
                                   final WebSocketSessionContext webSocketSessionContext);

    private WebSocketSessionContext getSessionContext(final String pathTemplate,
                                                      final org.springframework.web.reactive.socket.WebSocketSession session,
                                                      final HandshakeInfo handshakeInfo,
                                                      final Principal principal) {

        final String uriPath = handshakeInfo.getUri().getPath();
        final UriTemplate uriTemplate = new UriTemplate(pathTemplate);

        final var pathParameters = uriTemplate.match(uriPath);
        final var queryParameters = UriComponentsBuilder.fromUri(handshakeInfo.getUri()).build()
                .getQueryParams();
        final var headers = handshakeInfo.getHeaders();

        return WebSocketSessionContext.builder()
                .authentication(principal)
                .pathParameters(pathParameters)
                .queryParameters(queryParameters)
                .headers(headers)
                .sessionId(session.getId())
                .build();
    }

    boolean isPingPongEnabled() {
        return pingPongEnabled;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }

    public long getPingInterval() {
        return pingInterval;
    }
}
