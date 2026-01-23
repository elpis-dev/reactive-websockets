package io.github.elpis.reactive.websockets.handler;

import io.github.elpis.reactive.websockets.context.ReactiveWebSocketExceptionResolver;
import io.github.elpis.reactive.websockets.context.ReactiveWebsocketMessageEndpointResolver;
import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

public class ReflectiveWebSocketHandlerFactory {
  private static final Logger log =
      LoggerFactory.getLogger(ReflectiveWebSocketHandlerFactory.class);

  private final ReactiveWebsocketMessageEndpointResolver messageEndpointResolver;
  private final ReactiveWebSocketEventManagerFactory eventManagerFactory;
  private final ReactiveWebSocketSessionRegistry sessionRegistry;
  private final ReactiveHeartbeatFlowControlRegistry heartbeatFlowControlRegistry;
  private final ReactiveFlowControlChain flowControlChain;
  private final ReactiveWebSocketExceptionResolver exceptionResolver;
  private final ApplicationContext applicationContext;
  private final JsonMapper jsonMapper;

  public ReflectiveWebSocketHandlerFactory(
      final ReactiveWebsocketMessageEndpointResolver messageEndpointResolver,
      final ApplicationContext applicationContext) {

    Assert.notNull(messageEndpointResolver, "messageEndpointResolver must not be null");
    Assert.notNull(applicationContext, "applicationContext must not be null");
    this.applicationContext = applicationContext;
    this.messageEndpointResolver = messageEndpointResolver;

    this.eventManagerFactory =
        applicationContext.getBean(ReactiveWebSocketEventManagerFactory.class);
    Assert.notNull(
        this.eventManagerFactory, "ReactiveWebSocketEventManagerFactory bean must not be null");

    this.sessionRegistry = applicationContext.getBean(ReactiveWebSocketSessionRegistry.class);
    Assert.notNull(this.sessionRegistry, "ReactiveWebSocketSessionRegistry bean must not be null");

    this.heartbeatFlowControlRegistry =
        applicationContext.getBean(ReactiveHeartbeatFlowControlRegistry.class);
    Assert.notNull(
        this.heartbeatFlowControlRegistry,
        "ReactiveHeartbeatFlowControlRegistry bean must not be null");

    this.flowControlChain = applicationContext.getBean(ReactiveFlowControlChain.class);
    Assert.notNull(this.flowControlChain, "ReactiveFlowControlChain bean must not be null");

    this.exceptionResolver = applicationContext.getBean(ReactiveWebSocketExceptionResolver.class);
    Assert.notNull(
        this.exceptionResolver, "ReactiveWebSocketExceptionResolver bean must not be null");

    this.jsonMapper = applicationContext.getBean(JsonMapper.class);
    Assert.notNull(this.jsonMapper, "JsonMapper bean must not be null");
  }

  public List<BaseReactiveWebSocketHandler> createHandlers() {
    final Map<String, ReactiveWebsocketMessageEndpointResolver.MessageHandlerMethod>
        handlerMethods = messageEndpointResolver.getHandlerMethods();

    final List<BaseReactiveWebSocketHandler> handlers = new ArrayList<>();

    for (Map.Entry<String, ReactiveWebsocketMessageEndpointResolver.MessageHandlerMethod> entry :
        handlerMethods.entrySet()) {
      final String path = entry.getKey();
      final ReactiveWebsocketMessageEndpointResolver.MessageHandlerMethod handlerMethod =
          entry.getValue();
      final Object handlerBean =
          applicationContext.getBean(handlerMethod.beanName(), handlerMethod.beanType());
      final ReflectiveReactiveWebSocketHandler handler =
          this.createHandler(path, handlerMethod, handlerBean);
      handlers.add(handler);

      if (log.isDebugEnabled()) {
        log.debug(
            "Created reflection-based handler for path '{}' -> {}.{}()",
            path,
            handlerMethod.beanType().getSimpleName(),
            handlerMethod.method().getName());
      }
    }

    if (log.isInfoEnabled()) {
      log.info("Created {} reflection-based WebSocket handlers", handlers.size());
    }

    return handlers;
  }

  private ReflectiveReactiveWebSocketHandler createHandler(
      final String path,
      final ReactiveWebsocketMessageEndpointResolver.MessageHandlerMethod handlerMethod,
      final Object handlerBean) {

    return new ReflectiveReactiveWebSocketHandler(
        eventManagerFactory,
        sessionRegistry,
        jsonMapper,
        path,
        heartbeatFlowControlRegistry,
        flowControlChain,
        exceptionResolver,
        handlerMethod,
        handlerBean);
  }
}
