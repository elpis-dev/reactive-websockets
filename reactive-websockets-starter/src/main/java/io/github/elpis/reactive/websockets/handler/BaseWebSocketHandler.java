package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.config.CloseInitiator;
import io.github.elpis.reactive.websockets.config.SessionCloseInfo;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.ServerSessionClosedEvent;
import io.github.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.security.principal.Anonymous;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.CloseStatus;
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

    private final WebSocketEventManagerFactory eventManagerFactory;
    private final WebSocketSessionRegistry sessionRegistry;

    private final String pathTemplate;
    private final boolean heartbeatEnabled;
    private final long heartbeatInterval;
    private final long heartbeatTimeout;

    protected BaseWebSocketHandler(final WebSocketEventManagerFactory eventManagerFactory,
                                   final WebSocketSessionRegistry sessionRegistry,
                                   final String pathTemplate,
                                   final boolean heartbeatEnabled,
                                   final long heartbeatInterval,
                                   final long heartbeatTimeout) {

        this.eventManagerFactory = eventManagerFactory;
        this.sessionRegistry = sessionRegistry;

        this.pathTemplate = pathTemplate;
        this.heartbeatEnabled = heartbeatEnabled;
        this.heartbeatInterval = heartbeatInterval;
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public Mono<Void> handle(final org.springframework.web.reactive.socket.WebSocketSession session) {
        final WebSocketEventManager<ClientSessionClosedEvent> closedEventManager = this.eventManagerFactory
                .getEventManager(ClientSessionClosedEvent.class);
        final WebSocketEventManager<ServerSessionClosedEvent> serverSessionClosedEventManager = this.eventManagerFactory
                .getEventManager(ServerSessionClosedEvent.class);
        final WebSocketEventManager<SessionConnectedEvent> sessionConnectedEventManager = this.eventManagerFactory
                .getEventManager(SessionConnectedEvent.class);

        return Mono.deferContextual(Mono::just)
                .flatMap(contextView -> (Mono<String>) contextView.get("sessionId"))
                .flatMap(sessionId -> {
                    final HandshakeInfo handshakeInfo = session.getHandshakeInfo();

                    log.trace("Establishing WebSocketSession: id => {}, uri => {}, address => {}", sessionId,
                            handshakeInfo.getUri(), handshakeInfo.getRemoteAddress());

                    final ReactiveWebSocketSession reactiveWebSocketSession = ReactiveWebSocketSession.builder()
                            .isOpen(session::isOpen)
                            .sessionId(sessionId)
                            .onClose((eventSessionId, closeStatus) -> {
                                final ServerSessionClosedEvent event = this
                                        .getServerClosedEvent(eventSessionId, closeStatus);
                                serverSessionClosedEventManager.fire(event);
                            })
                            .build();

                    sessionConnectedEventManager.fire(SessionConnectedEvent.builder()
                            .webSocketSessionInfo(reactiveWebSocketSession)
                            .build());

                    this.sessionRegistry.save(reactiveWebSocketSession);

                    final Mono<Void> clientClosedSessionListener = session.closeStatus()
                            .doOnNext(closeStatus -> {
                                this.sessionRegistry.remove(sessionId);

                                final SessionCloseInfo sessionCloseInfo = SessionCloseInfo.builder()
                                        .closeStatus(closeStatus)
                                        .session(reactiveWebSocketSession)
                                        .build();
                                final ClientSessionClosedEvent event = new ClientSessionClosedEvent(sessionCloseInfo);

                                closedEventManager.fire(event);
                            }).then();

                    final Flux<Void> serverClosedSessionListener = serverSessionClosedEventManager
                            .asFlux()
                            .map(ServerSessionClosedEvent::payload)
                            .filter(sessionCloseInfo -> sessionCloseInfo.getSession().getSessionId().equals(sessionId))
                            .flatMap(sessionCloseInfo -> session.close(sessionCloseInfo.getCloseStatus()));

                    return session.getHandshakeInfo().getPrincipal()
                            .switchIfEmpty(Mono.just(new Anonymous()))
                            .flatMapMany(principal -> {
                                final WebSocketSessionContext webSocketSessionContext =
                                        this.getSessionContext(pathTemplate, sessionId, handshakeInfo, principal);
                                final Flux<Void> chain = this.buildChain(session, webSocketSessionContext);

                                return Flux.merge(chain, clientClosedSessionListener, serverClosedSessionListener);
                            })
                            .takeUntil(nothing -> session.isOpen())
                            .doOnError(throwable -> log.error(throwable.getMessage()))
                            .then();
                });
    }

    private ServerSessionClosedEvent getServerClosedEvent(final String sessionId, final CloseStatus closeStatus) {
        final ReactiveWebSocketSession webSocketSession = ReactiveWebSocketSession.builder()
                .sessionId(sessionId)
                .build();

        final SessionCloseInfo sessionCloseInfo = SessionCloseInfo.builder()
                .closeStatus(closeStatus)
                .session(webSocketSession)
                .closeInitiator(CloseInitiator.SERVER)
                .build();

       return new ServerSessionClosedEvent(sessionCloseInfo);
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
                                                      final String sessionId,
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
                .sessionId(sessionId)
                .build();
    }

    boolean isHeartbeatEnabled() {
        return heartbeatEnabled;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    protected WebSocketSessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }
}
