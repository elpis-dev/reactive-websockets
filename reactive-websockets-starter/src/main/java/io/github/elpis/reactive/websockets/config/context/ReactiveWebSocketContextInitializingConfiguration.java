package io.github.elpis.reactive.websockets.config.context;

import io.github.elpis.reactive.websockets.context.ReactiveWebSocketExceptionResolver;
import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveWebSocketContextInitializingConfiguration {
  @Bean(initMethod = "initialize")
  public ReactiveWebSocketExceptionResolver reactiveWebsocketExceptionResolver(
      final ApplicationContext applicationContext) {
    return new ReactiveWebSocketExceptionResolver(applicationContext);
  }

  @Bean(initMethod = "initialize")
  public ReactiveWebsocketMessageEndpointResolver reactiveWebsocketMessageEndpointProcessor(
      final ApplicationContext applicationContext) {
    return new ReactiveWebsocketMessageEndpointResolver(applicationContext);
  }
}
