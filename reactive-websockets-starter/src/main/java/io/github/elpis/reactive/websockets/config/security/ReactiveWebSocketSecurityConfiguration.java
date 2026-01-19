package io.github.elpis.reactive.websockets.config.security;

import io.github.elpis.reactive.websockets.security.ReactiveWebSocketHandshakeService;
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
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Lazy
@Configuration
public class ReactiveWebSocketSecurityConfiguration {

  /**
   * Creates a new {@link Bean @Bean} of type {@link WebSocketHandlerAdapter} with custom {@link
   * ReactiveWebSocketHandshakeService} if available.
   *
   * @param reactiveWebSocketHandshakeService custom implemented {@link
   *     ReactiveWebSocketHandshakeService} bean
   * @since 1.0.0
   */
  @Bean
  @ConditionalOnBean(ReactiveWebSocketHandshakeService.class)
  public WebSocketHandlerAdapter webSocketHandlerAdapter(
      final ReactiveWebSocketHandshakeService reactiveWebSocketHandshakeService) {
    return new WebSocketHandlerAdapter(reactiveWebSocketHandshakeService);
  }

  /**
   * Creates a new {@link Bean @Bean} of type {@link WebSocketHandlerAdapter} with default {@link
   * org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
   * HandshakeWebSocketService} if custom {@link ReactiveWebSocketHandshakeService} is not found.
   *
   * @since 1.0.0
   */
  @Bean
  @ConditionalOnMissingBean(ReactiveWebSocketHandshakeService.class)
  public WebSocketHandlerAdapter defaultWebSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

  /**
   * Setups a {@link SecurityWebFilterChain} that allows all websocket connections per {@link
   * SimpleUrlHandlerMapping} paths registered. Disabled by default. If needed to be enabled set:
   * {@code spring.reactive.websocket.security.excludeWebFilterChains = true} at your .properties
   * file or at .yml:
   *
   * <pre>
   * spring:
   *  reactive:
   *    websocket:
   *      security:
   *          excludeWebFilterChains: true
   * </pre>
   *
   * @since 1.0.0
   */
  @Bean("defaultWebSocketFilterChain")
  @ConditionalOnProperty(
      value = "spring.reactive.websocket.security.excludeWebFilterChains",
      havingValue = "true")
  public SecurityWebFilterChain defaultWebSocketFilterChain(
      final ServerHttpSecurity security, final ApplicationContext context) {
    final SimpleUrlHandlerMapping handlerMapping =
        context.getBean("handlerMapping", SimpleUrlHandlerMapping.class);

    return security
        .securityMatcher(
            new NegatedServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                    handlerMapping.getUrlMap().keySet().stream()
                        .map(url -> url + "/**")
                        .toArray(String[]::new))))
        .build();
  }
}
