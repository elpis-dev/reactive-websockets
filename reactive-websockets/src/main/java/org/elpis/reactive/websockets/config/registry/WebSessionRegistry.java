package org.elpis.reactive.websockets.config.registry;

import io.micrometer.core.instrument.Gauge;
import org.elpis.reactive.websockets.config.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.event.manager.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.elpis.reactive.websockets.mertics.WebSocketMetricsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.elpis.reactive.websockets.mertics.WebSocketMetricsService.MeterConstants.ACTIVE_SESSIONS;

@Component
public class WebSessionRegistry extends ConcurrentHashMap<String, WebSocketSessionInfo> {
    private final transient ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final transient WebSocketEventManager<SessionConnectedEvent> webSocketConnectionEvent;
    private final transient WebSocketEventManager<ClientSessionClosedEvent> closedEventWebSocketEventManager;
    private final transient WebSocketMetricsService webSocketMetricsService;

    public WebSessionRegistry(final WebSocketEventManager<SessionConnectedEvent> webSocketConnectionEvent,
                              final WebSocketEventManager<ClientSessionClosedEvent> closedEventWebSocketEventManager,
                              final WebSocketMetricsService webSocketMetricsService) {

        this.webSocketConnectionEvent = webSocketConnectionEvent;
        this.closedEventWebSocketEventManager = closedEventWebSocketEventManager;
        this.webSocketMetricsService = webSocketMetricsService;
    }

    @Override
    public WebSocketSessionInfo put(@NotNull String key, @NotNull WebSocketSessionInfo value) {
        final WebSocketSessionInfo webSocketSessionInfo = super.put(key, value);

        webSocketConnectionEvent.fire(SessionConnectedEvent.builder()
                .webSocketSessionInfo(value)
                .build());

        return webSocketSessionInfo;
    }

    @PostConstruct
    private void post() {
        this.webSocketMetricsService.withGauge(this, (sessionInfoMap, meterRegistry) ->
                Gauge.builder(ACTIVE_SESSIONS.getKey(), sessionInfoMap, Map::size)
                        .description(ACTIVE_SESSIONS.getDescription())
                        .register(meterRegistry));

        this.executorService.submit(() -> this.webSocketConnectionEvent.asFlux()
                .map(SessionConnectedEvent::payload)
                .subscribe(webSocketSessionInfo -> webSocketSessionInfo.getCloseStatus()
                        .subscribe(closeStatus -> {
                            final ClientSessionClosedEvent clientSessionClosedEvent = ClientSessionClosedEvent.builder()
                                    .clientSessionCloseInfo(ClientSessionCloseInfo.builder()
                                            .sessionInfo(webSocketSessionInfo)
                                            .closeStatus(closeStatus)
                                            .build())
                                    .build();

                            this.closedEventWebSocketEventManager.fire(clientSessionClosedEvent);
                        }))
        );
    }
}
