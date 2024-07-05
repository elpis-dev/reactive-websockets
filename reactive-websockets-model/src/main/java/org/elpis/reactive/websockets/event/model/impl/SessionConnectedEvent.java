package org.elpis.reactive.websockets.event.model.impl;

import org.elpis.reactive.websockets.config.handler.WebSocketSessionInfo;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

public class SessionConnectedEvent implements WebSocketEvent<WebSocketSessionInfo> {
    private WebSocketSessionInfo webSocketSessionInfo;

    @Override
    public WebSocketSessionInfo payload() {
        return webSocketSessionInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SessionConnectedEvent sessionConnectedEvent = new SessionConnectedEvent();

        public Builder webSocketSessionInfo(WebSocketSessionInfo webSocketSessionInfo) {
            this.sessionConnectedEvent.webSocketSessionInfo = webSocketSessionInfo;
            return this;
        }

        public SessionConnectedEvent build() {
            return this.sessionConnectedEvent;
        }
    }

}
