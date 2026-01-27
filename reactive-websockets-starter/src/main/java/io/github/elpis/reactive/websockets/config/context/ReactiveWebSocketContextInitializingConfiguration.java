package io.github.elpis.reactive.websockets.config.context;

import io.github.elpis.reactive.websockets.context.ReactiveWebSocketClosedEventHandlersResolver;
import io.github.elpis.reactive.websockets.context.ReactiveWebSocketExceptionResolver;
import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for initializing Reactive WebSocket context components.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@Configuration
public class ReactiveWebSocketContextInitializingConfiguration {

  /**
   * Bean for resolving WebSocket exceptions in a reactive context.
   *
   * @param applicationContext the Spring application context
   * @return the ReactiveWebSocketExceptionResolver bean
   */
  @Bean(initMethod = "initialize")
  public ReactiveWebSocketExceptionResolver reactiveWebsocketExceptionResolver(
      final ApplicationContext applicationContext) {
    return new ReactiveWebSocketExceptionResolver(applicationContext);
  }

  /**
   * Bean for resolving WebSocket message endpoints in a reactive context.
   *
   * @param applicationContext the Spring application context
   * @return the ReactiveWebsocketMessageEndpointResolver bean
   */
  @Bean(initMethod = "initialize")
  public ReactiveWebsocketMessageEndpointResolver reactiveWebsocketMessageEndpointProcessor(
      final ApplicationContext applicationContext) {
    return new ReactiveWebsocketMessageEndpointResolver(applicationContext);
  }

  /**
   * Bean for resolving WebSocket closed event handlers in a reactive context.
   *
   * @param applicationContext the Spring application context
   * @return the ReactiveWebSocketClosedEventHandlersResolver bean
   */
  @Bean(initMethod = "initialize")
  public ReactiveWebSocketClosedEventHandlersResolver reactiveWebSocketClosedEventHandlersResolver(
      final ApplicationContext applicationContext) {
    return new ReactiveWebSocketClosedEventHandlersResolver(applicationContext);
  }
}
