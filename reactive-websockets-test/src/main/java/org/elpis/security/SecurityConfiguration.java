package org.elpis.security;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableReactiveSocketSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http.authorizeExchange().anyExchange().permitAll().and().build();
    }

    @Bean
    public SocketHandshakeService socketHandshakeService() {
        return new WebSocketHandshakeService();
    }
}
