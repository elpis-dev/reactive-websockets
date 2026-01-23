package io.github.elpis.reactive.websockets.config.session;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionMaintenanceService;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveWebSocketSessionConfiguration {

  @Bean
  public ReactiveWebSocketSessionRegistry webSocketSessionRegistry() {
    return new ReactiveWebSocketSessionRegistry();
  }

  @Bean
  public ReactiveWebSocketSessionMaintenanceService sessionMaintenanceService() {
    return new ReactiveWebSocketSessionMaintenanceService(webSocketSessionRegistry());
  }
}
