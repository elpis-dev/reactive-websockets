package org.elpis.reactive.websockets.config;

import io.micrometer.core.instrument.Gauge;
import org.elpis.reactive.websockets.event.WebSocketEventManager;
import org.elpis.reactive.websockets.event.model.impl.SessionConnectedEvent;
import org.elpis.reactive.websockets.mertics.WebSocketMetricsService;
import org.elpis.reactive.websockets.web.model.WebSocketSessionInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.elpis.reactive.websockets.mertics.WebSocketMetricsService.MeterConstants.ACTIVE_SESSIONS;

@Component
public class WebSessionRegistry extends ConcurrentHashMap<String, WebSocketSessionInfo> {
    private final transient WebSocketEventManager<SessionConnectedEvent> webSocketConnectionEvent;
    private final transient WebSocketMetricsService webSocketMetricsService;

    public WebSessionRegistry(final WebSocketEventManager<SessionConnectedEvent> webSocketConnectionEvent,
                              final WebSocketMetricsService webSocketMetricsService) {

        this.webSocketConnectionEvent = webSocketConnectionEvent;
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
    }
}
