package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves and registers reactive WebSocket handlers based on provided handler functions.
 *
 * <p>This class takes a list of {@link ReactiveWebSocketHandlerFunction} instances and uses them to
 * create and register corresponding {@link BaseReactiveWebSocketHandler} instances. It utilizes
 * various dependencies such as event manager factories, session registries, and flow control
 * registries to properly configure each handler.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveWebSocketHandlerRouteResolver {
  private final ReactiveWebSocketEventManagerFactory eventManagerFactory;
  private final ReactiveWebSocketSessionRegistry sessionRegistry;
  private final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry;
  private final ReactiveFlowControlChain reactiveFlowControlChain;
  private final JsonMapper jsonMapper;
  private final List<ReactiveWebSocketHandlerFunction> functions;

  public ReactiveWebSocketHandlerRouteResolver(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain,
      final JsonMapper jsonMapper,
      final List<ReactiveWebSocketHandlerFunction> functions) {

    this.eventManagerFactory = eventManagerFactory;
    this.sessionRegistry = sessionRegistry;
    this.reactiveHeartbeatFlowControlRegistry = reactiveHeartbeatFlowControlRegistry;
    this.reactiveFlowControlChain = reactiveFlowControlChain;
    this.functions = functions;
    this.jsonMapper = jsonMapper;
  }

  /**
   * Resolves and registers WebSocket handlers based on the provided handler functions.
   *
   * @return a list of registered {@link BaseReactiveWebSocketHandler} instances
   */
  public List<BaseReactiveWebSocketHandler> resolve() {
    final List<BaseReactiveWebSocketHandler> handlers = new ArrayList<>();

    functions.forEach(
        webSocketHandlerFunction -> {
          ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive function =
              (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
                  webSocketHandlerFunction;
          while (function.getNext() != null) {
            final BaseReactiveWebSocketHandler webSocketHandler =
                function.register(
                    eventManagerFactory,
                    sessionRegistry,
                    jsonMapper,
                    reactiveHeartbeatFlowControlRegistry,
                    reactiveFlowControlChain);
            if (webSocketHandler == null) {
              break;
            }

            handlers.add(webSocketHandler);
            function =
                (ReactiveWebSocketHandlerFunctions.DefaultRouterFunctionReactive)
                    function.getNext();
          }

          Optional.ofNullable(
                  function.register(
                      eventManagerFactory,
                      sessionRegistry,
                      jsonMapper,
                      reactiveHeartbeatFlowControlRegistry,
                      reactiveFlowControlChain))
              .ifPresent(handlers::add);
        });

    return handlers;
  }
}
