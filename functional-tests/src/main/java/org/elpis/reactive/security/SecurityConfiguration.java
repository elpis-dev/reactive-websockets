package org.elpis.reactive.security;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.WebFilter;

import java.util.List;

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
    public WebFilter anonymousFilter() {
        return new AnonymousAuthenticationWebFilter("key", new Anonymous(), List.of(new SimpleGrantedAuthority("role")));
    }

    @Bean
    public SocketHandshakeService socketHandshakeService() {
        return SocketHandshakeService.builder()
                .handshake(anonymousFilter()::filter)
                .requestUpgradeStrategy(new ReactorNettyRequestUpgradeStrategy())
                .build();
    }

}
