package org.elpis.reactive.websockets.event.model.impl;

import lombok.Builder;
import org.elpis.reactive.websockets.event.model.WebSocketEvent;
import org.elpis.reactive.websockets.web.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.web.model.WebSocketSessionInfo;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

@Builder
public class ClientSessionClosedEvent implements WebSocketEvent<ClientSessionCloseInfo> {
    private final ClientSessionCloseInfo clientSessionCloseInfo;

    @Override
    public ClientSessionCloseInfo event() {
        return clientSessionCloseInfo;
    }
}
