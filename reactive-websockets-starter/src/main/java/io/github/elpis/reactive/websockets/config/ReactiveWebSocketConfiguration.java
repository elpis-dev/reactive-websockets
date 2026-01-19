package io.github.elpis.reactive.websockets.config;

import static io.github.elpis.reactive.websockets.Constants.HANDLER_ORDER;

import io.github.elpis.reactive.websockets.config.context.ReactiveWebSocketContextInitializingConfiguration;
import io.github.elpis.reactive.websockets.config.context.ReactiveWebSocketParameterResolverConfiguration;
import io.github.elpis.reactive.websockets.config.event.ReactiveWebSocketEventConfiguration;
import io.github.elpis.reactive.websockets.config.flowcontrol.ReactiveWebSocketFlowControlConfiguration;
import io.github.elpis.reactive.websockets.config.maintenance.ReactiveWebSocketRegistryMaintenanceConfig;
import io.github.elpis.reactive.websockets.config.mapper.ReactiveWebSocketMappingConfiguration;
import io.github.elpis.reactive.websockets.config.session.ReactiveWebSocketSessionConfiguration;
import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.exception.WebSocketMappingException;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.ReflectiveWebSocketHandlerFactory;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.handler.route.ReactiveWebSocketHandlerFunction;
import io.github.elpis.reactive.websockets.handler.route.ReactiveWebSocketHandlerFunctions;
import io.github.elpis.reactive.websockets.handler.route.ReactiveWebSocketHandlerRouteResolver;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebSession;
import reactor.util.context.Context;

/**
 * Configuration class that setups all the websocket endpoints and processes annotated methods.
 *
 * @author Phillip J. Fry
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
@Configuration
@Import({
  ReactiveWebSocketFlowControlConfiguration.class,
  ReactiveWebSocketEventConfiguration.class,
  ReactiveWebSocketSessionConfiguration.class,
  ReactiveWebSocketRegistryMaintenanceConfig.class,
  ReactiveWebSocketMappingConfiguration.class,
  ReactiveWebSocketParameterResolverConfiguration.class,
  ReactiveWebSocketContextInitializingConfiguration.class
})
public class ReactiveWebSocketConfiguration {

  @Bean
  @ConditionalOnMissingBean(ReactiveWebSocketHandlerFunction.class)
  public ReactiveWebSocketHandlerFunction webSocketRouterFunction() {
    return ReactiveWebSocketHandlerFunctions.empty();
  }

  @Bean
  public WebFilter sessionFilter() {
    return (exchange, chain) ->
        chain
            .filter(exchange)
            .contextWrite(Context.of("sessionId", exchange.getSession().map(WebSession::getId)));
  }

  @Bean
  public ReflectiveWebSocketHandlerFactory reflectiveWebSocketHandlerFactory(
      final ReactiveWebsocketMessageEndpointResolver messageEndpointResolver,
      final ApplicationContext applicationContext) {
    return new ReflectiveWebSocketHandlerFactory(messageEndpointResolver, applicationContext);
  }

  /**
   * {@link HandlerMapping} bean with all {@link OnMessage} resource.
   *
   * @return {@link HandlerMapping}
   * @since 1.0.0
   */
  @Bean
  public HandlerMapping handlerMapping(
      final List<BaseReactiveWebSocketHandler> handlers,
      final ReactiveWebSocketHandlerRouteResolver routeResolver,
      final ReflectiveWebSocketHandlerFactory handlerFactory) {

    final Map<String, WebSocketHandler> handlerMap = new HashMap<>();

    final List<BaseReactiveWebSocketHandler> routeHandlers = routeResolver.resolve();
    final List<BaseReactiveWebSocketHandler> reflectionHandlers = handlerFactory.createHandlers();
    Stream.concat(
            Stream.concat(handlers.stream(), routeHandlers.stream()), reflectionHandlers.stream())
        .forEach(
            handler -> {
              if (handlerMap.putIfAbsent(handler.getPathTemplate(), handler) != null) {
                throw new WebSocketMappingException(
                    "WebSocketHandler with path %s was already registered",
                    handler.getPathTemplate());
              }
            });

    return new SimpleUrlHandlerMapping(handlerMap, HANDLER_ORDER);
  }

  @Bean
  public ReactiveWebSocketTemplate webSocketTemplate(
      final ReactiveWebSocketSessionRegistry registry,
      @Value("${broadcast.concurrency:32}") final int broadcastConcurrency) {

    return new ReactiveWebSocketTemplate(registry, broadcastConcurrency);
  }

  @Bean
  public ReactiveWebSocketHandlerRouteResolver webSocketHandlerRouteResolver(
      final ReactiveWebSocketSessionRegistry registry,
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain,
      final List<ReactiveWebSocketHandlerFunction> functions) {
    return new ReactiveWebSocketHandlerRouteResolver(
        eventManagerFactory,
        registry,
        reactiveHeartbeatFlowControlRegistry,
        reactiveFlowControlChain,
        functions);
  }
}
