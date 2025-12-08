package io.github.elpis.reactive.websockets.event.model.impl;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.event.model.WebSocketEvent;

public class SessionConnectedEvent implements WebSocketEvent<ReactiveWebSocketSession> {
    private ReactiveWebSocketSession webSocketSessionInfo;

    @Override
    public ReactiveWebSocketSession payload() {
        return webSocketSessionInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SessionConnectedEvent sessionConnectedEvent = new SessionConnectedEvent();

        public Builder webSocketSessionInfo(ReactiveWebSocketSession webSocketSessionInfo) {
            this.sessionConnectedEvent.webSocketSessionInfo = webSocketSessionInfo;
            return this;
        }

        public SessionConnectedEvent build() {
            return this.sessionConnectedEvent;
        }
    }

}
