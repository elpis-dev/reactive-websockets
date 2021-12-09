package org.elpis.reactive.websockets.config.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.springframework.web.reactive.socket.CloseStatus;

@Builder
@Getter
@Setter
public class ClientSessionCloseInfo {
    private WebSocketSessionInfo webSocketSessionInfo;
    private CloseStatus closeStatus;
}
