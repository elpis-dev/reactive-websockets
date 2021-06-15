package org.elpis.security;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

@EnableReactiveSocketSecurity
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http.authorizeExchange()
                .anyExchange().permitAll()
                .and()
                .formLogin()
                .disable()
                .build();
    }

    @Bean
    public SocketHandshakeService socketHandshakeService(final SecurityWebFilterChain securityWebFilterChain) {
        return SocketHandshakeService.builder()
                .securityChain(() -> securityWebFilterChain)
                .requestUpgradeStrategy(new ReactorNettyRequestUpgradeStrategy())
                .handleError(throwable -> ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(throwable.getMessage()))
                .build();
    }
}
