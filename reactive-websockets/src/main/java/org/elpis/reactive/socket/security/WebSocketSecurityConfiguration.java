package org.elpis.reactive.socket.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Lazy
@Configuration
public class WebSocketSecurityConfiguration {

    @Bean
    @ConditionalOnBean(SocketHandshakeService.class)
    public WebSocketHandlerAdapter webSocketHandlerAdapter(final SocketHandshakeService socketHandshakeService) {
        return new WebSocketHandlerAdapter(socketHandshakeService);
    }

    @Bean
    @ConditionalOnMissingBean(SocketHandshakeService.class)
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}
