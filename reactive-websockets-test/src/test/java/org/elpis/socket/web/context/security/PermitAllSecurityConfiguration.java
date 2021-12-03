package org.elpis.socket.web.context.security;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveSocketSecurity
@Profile(SecurityProfiles.PERMIT_ALL)
public class PermitAllSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange().anyExchange().permitAll().and().build();
    }

}