package org.elpis.reactive.websockets.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

/**
 * Configuration class that setups a basic security principles for websocket connection handshake.
 *
 * @author Alex Zharkov
 * @see org.springframework.context.annotation.Configuration
 * @since 0.1.0
 */
@Lazy
@Configuration
public final class WebSocketSecurityConfiguration {

    /**
     * Creates a new {@link Bean @Bean} of type {@link WebSocketHandlerAdapter} with custom {@link SocketHandshakeService} if available.
     *
     * @param socketHandshakeService custom implemented {@link SocketHandshakeService} bean
     * @since 0.1.0
     */
    @Bean
    @ConditionalOnBean(SocketHandshakeService.class)
    public WebSocketHandlerAdapter webSocketHandlerAdapter(final SocketHandshakeService socketHandshakeService) {
        return new WebSocketHandlerAdapter(socketHandshakeService);
    }

    /**
     * Creates a new {@link Bean @Bean} of type {@link WebSocketHandlerAdapter} with default {@link org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService HandshakeWebSocketService} if custom {@link SocketHandshakeService} is not found.
     *
     * @since 0.1.0
     */
    @Bean
    @ConditionalOnMissingBean(SocketHandshakeService.class)
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    /**
     * Setups a {@link SecurityWebFilterChain} that allows all websocket connections per {@link SimpleUrlHandlerMapping} paths registered. Disabled by default. If needed to be enabled set:
     * {@code spring.reactive.socket.security.excludeWebFilterChains = true} at your .properties file or at .yml:
     * <pre>
     * spring:
     *  reactive:
     *    socket:
     *      security:
     *          excludeWebFilterChains: true
     * </pre>
     *
     * @since 0.1.0
     */
    @Bean("defaultWebSocketFilterChain")
    @ConditionalOnProperty(value = "spring.reactive.socket.security.excludeWebFilterChains", havingValue = "true")
    public SecurityWebFilterChain defaultWebSocketFilterChain(final ServerHttpSecurity security, final ApplicationContext context) {
        final SimpleUrlHandlerMapping handlerMapping = context.getBean("handlerMapping", SimpleUrlHandlerMapping.class);

        return security.securityMatcher(new NegatedServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(handlerMapping.getUrlMap().keySet()
                        .stream()
                        .map(url -> url + "/**")
                        .toArray(String[]::new))
        )).build();
    }

}
