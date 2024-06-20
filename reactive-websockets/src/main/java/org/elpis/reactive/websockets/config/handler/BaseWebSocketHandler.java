package org.elpis.reactive.websockets.config.handler;

import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.*;
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
    private final JsonMapper jsonMapper;

    protected BaseWebSocketHandler(String pathTemplate,
                                   WebSessionRegistry sessionRegistry,
                                   boolean pingPongEnabled,
                                   long pingInterval,
                                   JsonMapper jsonMapper) {

        this.pathTemplate = pathTemplate;
        this.sessionRegistry = sessionRegistry;
        this.pingPongEnabled = pingPongEnabled;
        this.pingInterval = pingInterval;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Mono<Void> handle(final WebSocketSession session) {
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
                .closeStatus(session.closeStatus())
                .build();

        this.sessionRegistry.put(session.getId(), webSocketSessionInfo);

        return session.getHandshakeInfo().getPrincipal()
                .switchIfEmpty(Mono.just(new Anonymous()))
                .flatMapMany(principal -> {
                    final WebSocketSessionContext webSocketSessionContext =
                            this.getSessionContext(pathTemplate, session, handshakeInfo, principal);
                    return this.buildChain(session, webSocketSessionContext);
                })
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    Flux<WebSocketMessage> mapOutput(final WebSocketSession session, final Publisher<?> publisher) {
        return Flux.from(publisher)
                .flatMap(any -> {
                    if (any instanceof WebSocketMessage) {
                        return Flux.just((WebSocketMessage) any);
                    } else if (CloseStatus.class.isAssignableFrom(any.getClass())) {
                        return session.close(TypeUtils.cast(any, CloseStatus.class)).cast(WebSocketMessage.class);
                    } else if (any instanceof byte[]) {
                        return Mono.just(session.binaryMessage(factory -> factory.wrap((byte[]) any)));
                    } else if (InputStream.class.isAssignableFrom(any.getClass())) {
                        return DataBufferUtils.readByteChannel(() ->
                                                Channels.newChannel((InputStream) any),
                                        DefaultDataBufferFactory.sharedInstance, 4096)
                                .map(dataBuffer -> session.binaryMessage(factory -> dataBuffer));
                    }

                    return this.jsonMapper.applyWithFlux(any).map(session::textMessage);
                });
    }

    public Publisher<?> apply(final WebSocketSessionContext context, final Flux<WebSocketMessage> messages) {
        //do nothing, waiting for override
        return null;
    }

    public void run(final WebSocketSessionContext context, final Flux<WebSocketMessage> messages) {
        //do nothing, waiting for override
    }

    abstract Flux<Void> buildChain(final WebSocketSession webSocketSession,
                                   final WebSocketSessionContext webSocketSessionContext);

    private WebSocketSessionContext getSessionContext(final String pathTemplate,
                                                      final WebSocketSession session,
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
