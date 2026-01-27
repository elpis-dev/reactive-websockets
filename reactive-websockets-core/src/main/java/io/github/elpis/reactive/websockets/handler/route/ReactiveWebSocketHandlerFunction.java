package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;

/**
 * Functional interface for defining reactive WebSocket handler functions. These functions can be
 * composed to create complex routing logic for WebSocket messages. Each function is responsible for
 * registering a {@link BaseReactiveWebSocketHandler} with the provided dependencies.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@FunctionalInterface
public interface ReactiveWebSocketHandlerFunction {
  /**
   * Registers a {@link BaseReactiveWebSocketHandler} using the provided dependencies.
   *
   * @param eventManagerFactory the factory for creating WebSocket event managers
   * @param sessionRegistry the registry for managing WebSocket sessions
   * @param jsonMapper the JSON mapper for serializing and deserializing messages
   * @param reactiveHeartbeatFlowControlRegistry the registry for heartbeat flow control policies
   * @param reactiveFlowControlChain the chain of flow control mechanisms
   * @return a configured {@link BaseReactiveWebSocketHandler}
   */
  BaseReactiveWebSocketHandler register(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain);

  /**
   * Chains another WebSocket handler function to the current one.
   *
   * @param path the path for the WebSocket handler
   * @param function the void handler function to be executed
   * @return a new {@link ReactiveWebSocketHandlerFunction} that combines both handlers
   */
  default <T> ReactiveWebSocketHandlerFunction handle(
      final String path,
      final ReactiveWebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

    final ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive webSocketHandlerFunction =
        (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
            ReactiveWebSocketHandlerFunctions.handle(path, function);

    return webSocketHandlerFunction.setNext(this);
  }

  /**
   * Chains another WebSocket handler function to the current one.
   *
   * @param path the path for the WebSocket handler
   * @param function the void handler function to be executed
   * @return a new {@link ReactiveWebSocketHandlerFunction} that combines both handlers
   */
  default ReactiveWebSocketHandlerFunction handle(
      final String path,
      final ReactiveWebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

    final ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive webSocketHandlerFunction =
        (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
            ReactiveWebSocketHandlerFunctions.handle(path, function);

    return webSocketHandlerFunction.setNext(this);
  }

  /**
   * Chains another WebSocket handler function to the current one.
   *
   * @param another the other WebSocket handler function to chain
   * @return a new {@link ReactiveWebSocketHandlerFunction} that combines both handlers
   */
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
