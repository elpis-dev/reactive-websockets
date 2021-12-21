package org.elpis.reactive.websockets.event.model.impl;

import lombok.Builder;
import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;

@Builder
public class SessionConnectedEvent implements WebSocketEvent<WebSocketSessionInfo> {
    private final WebSocketSessionInfo webSocketSessionInfo;

    @Override
    public WebSocketSessionInfo payload() {
        return webSocketSessionInfo;
    }
}
