package org.elpis.reactive.websockets.config.handler;

import jakarta.annotation.PostConstruct;
import org.elpis.reactive.websockets.config.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.event.CloseMessage;
import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class WebSessionRegistry extends ConcurrentHashMap<String, WebSocketSessionInfo> {
    private static final Logger log = LoggerFactory.getLogger(WebSessionRegistry.class);

    private final transient ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final transient WebSocketEventManager<SessionConnectedEvent> webSocketConnectionEvent;
    private final transient WebSocketEventManager<ClientSessionClosedEvent> closedEventWebSocketEventManager;

    private final transient Sinks.Many<CloseMessage> closeMessageSink = Sinks.many()
            .multicast()
            .onBackpressureBuffer();

    public WebSessionRegistry(final WebSocketEventManager<SessionConnectedEvent> webSocketConnectionEvent,
                              final WebSocketEventManager<ClientSessionClosedEvent> closedEventWebSocketEventManager) {

        this.webSocketConnectionEvent = webSocketConnectionEvent;
        this.closedEventWebSocketEventManager = closedEventWebSocketEventManager;
    }

    public boolean close(final String sessionId, final CloseStatus closeStatus) {
        final CloseMessage closeMessage = new CloseMessage();
        closeMessage.setSessionId(sessionId);
        closeMessage.setCloseStatus(closeStatus);

        return this.closeMessageSink.tryEmitNext(closeMessage) == Sinks.EmitResult.OK;
    }

    Flux<CloseMessage> listen() {
        return this.closeMessageSink.asFlux().share();
    }

    @Override
    public WebSocketSessionInfo put(final String sessionId, final WebSocketSessionInfo session) {
        final WebSocketSessionInfo webSocketSessionInfo = super.put(sessionId, session);

        final Sinks.EmitResult result = webSocketConnectionEvent.fire(SessionConnectedEvent.builder()
                .webSocketSessionInfo(session)
                .build());

        if (result.isFailure()) {
            log.warn("Unable to fire a new SessionConnectedEvent for session {}: {}", session.getId(), result);
        } else {
            log.debug("Successfully emitted new SessionConnectedEvent for session {}", session.getId());
        }

        return webSocketSessionInfo;
    }

    @PostConstruct
    private void post() {
        this.executorService.submit(() -> this.webSocketConnectionEvent.asFlux()
                .map(SessionConnectedEvent::payload)
                .subscribe(webSocketSessionInfo -> webSocketSessionInfo.getCloseStatus()
                        .subscribe(closeStatus -> {
                            log.debug("Received a session `{}` close trigger with code: {}", webSocketSessionInfo.getId(), closeStatus.getCode());

                            final ClientSessionClosedEvent clientSessionClosedEvent = ClientSessionClosedEvent.builder()
                                    .clientSessionCloseInfo(ClientSessionCloseInfo.builder()
                                            .sessionInfo(webSocketSessionInfo)
                                            .closeStatus(closeStatus)
                                            .build())
                                    .build();

                            final Sinks.EmitResult result = this.closedEventWebSocketEventManager.fire(clientSessionClosedEvent);
                            if (result.isFailure()) {
                                log.warn("Unable to fire a new ClientSessionClosedEvent for session {}: {}", webSocketSessionInfo.getId(), result);
                            } else {
                                log.debug("Successfully emitted new ClientSessionClosedEvent for session {}", webSocketSessionInfo.getId());
                            }

                            this.remove(webSocketSessionInfo.getId());
                        }))
        );
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
