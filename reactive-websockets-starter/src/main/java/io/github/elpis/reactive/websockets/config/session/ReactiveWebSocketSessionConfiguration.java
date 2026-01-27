package io.github.elpis.reactive.websockets.config.session;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionMaintenanceService;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for managing reactive WebSocket sessions.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
public class ReactiveWebSocketSessionConfiguration {

  /**
   * Creates a bean for ReactiveWebSocketSessionRegistry to manage WebSocket sessions.
   *
   * @return the ReactiveWebSocketSessionRegistry bean
   */
  @Bean
  public ReactiveWebSocketSessionRegistry webSocketSessionRegistry() {
    return new ReactiveWebSocketSessionRegistry();
  }

  /**
   * Creates a bean for ReactiveWebSocketSessionMaintenanceService to maintain WebSocket sessions.
   *
   * @return the ReactiveWebSocketSessionMaintenanceService bean
   */
  @Bean
  public ReactiveWebSocketSessionMaintenanceService sessionMaintenanceService() {
    return new ReactiveWebSocketSessionMaintenanceService(webSocketSessionRegistry());
  }
}
