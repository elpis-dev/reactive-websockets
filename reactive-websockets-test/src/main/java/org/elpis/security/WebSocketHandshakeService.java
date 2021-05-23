package org.elpis.security;

import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class WebSocketHandshakeService extends SocketHandshakeService {
    public WebSocketHandshakeService() {
        super(new ReactorNettyRequestUpgradeStrategy());
    }

    @Override
    public Mono<?> handshake(@NonNull final ServerWebExchange exchange) {
        return super.handshake(exchange);
    }
}
