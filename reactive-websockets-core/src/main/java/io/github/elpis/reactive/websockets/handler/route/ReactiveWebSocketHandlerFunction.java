package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;

// TODO: Check if it's consistent with the latest API changes
@FunctionalInterface
public interface ReactiveWebSocketHandlerFunction {
  BaseReactiveWebSocketHandler register(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain);

  default <T> ReactiveWebSocketHandlerFunction handle(
      final String path,
      final ReactiveWebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

    final ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive webSocketHandlerFunction =
        (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
            ReactiveWebSocketHandlerFunctions.handle(path, function);

    return webSocketHandlerFunction.setNext(this);
  }

  default ReactiveWebSocketHandlerFunction handle(
      final String path,
      final ReactiveWebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

    final ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive webSocketHandlerFunction =
        (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
            ReactiveWebSocketHandlerFunctions.handle(path, function);

    return webSocketHandlerFunction.setNext(this);
  }

  default ReactiveWebSocketHandlerFunction and(final ReactiveWebSocketHandlerFunction another) {
    ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive webSocketHandlerFunction =
        (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive) another;
    while (webSocketHandlerFunction.getNext() != null) {
      webSocketHandlerFunction =
          (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
              webSocketHandlerFunction.getNext();
    }
    webSocketHandlerFunction.setNext(this);

    return another;
  }
}
