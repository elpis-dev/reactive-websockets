package org.elpis.reactive.websockets.web.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

@Builder
@Getter
@Setter
public class ClientSessionCloseInfo {
    private WebSocketSessionInfo webSocketSessionInfo;
    private Mono<CloseStatus> closeStatus;
}
