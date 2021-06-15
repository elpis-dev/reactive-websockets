package org.elpis.reactive.websockets.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.List;
import java.util.stream.Collectors;

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

    @Bean("defaultWebSocketFilterChain")
    @ConditionalOnProperty(value = "spring.reactive.socket.security.excludeWebFilterChains", havingValue = "true")
    @ConditionalOnBean(SimpleUrlHandlerMapping.class)
    public SecurityWebFilterChain defaultWebSocketFilterChain(final ServerHttpSecurity serverHttpSecurity,
                                                              final SimpleUrlHandlerMapping simpleUrlHandlerMapping) {


        return serverHttpSecurity.securityMatcher(new NegatedServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(simpleUrlHandlerMapping.getUrlMap().keySet()
                        .stream()
                        .map(url -> url + "/**")
                        .toArray(String[]::new))
        )).build();
    }

}
