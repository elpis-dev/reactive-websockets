package io.github.elpis.reactive.websockets.session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class WebSocketSessionRegistry {

    private final Map<String, ReactiveWebSocketSession> sessionRegistry = new ConcurrentHashMap<>();

    public ReactiveWebSocketSession save(final ReactiveWebSocketSession session) {
        return this.sessionRegistry.put(session.getSessionId(), session);
    }

    public Optional<ReactiveWebSocketSession> get(final String sessionId) {
        return Optional.ofNullable(this.sessionRegistry.get(sessionId));
    }

    public Optional<ReactiveWebSocketSession> remove(String sessionId) {
        return Optional.ofNullable(this.sessionRegistry.get(sessionId));
    }

}
