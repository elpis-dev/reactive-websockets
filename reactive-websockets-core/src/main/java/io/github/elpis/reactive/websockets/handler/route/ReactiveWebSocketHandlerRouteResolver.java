package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Check if it's consistent with the latest API changes
public class ReactiveWebSocketHandlerRouteResolver {
  private final ReactiveWebSocketEventManagerFactory eventManagerFactory;
  private final ReactiveWebSocketSessionRegistry sessionRegistry;
  private final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry;
  private final ReactiveFlowControlChain reactiveFlowControlChain;
  private final List<ReactiveWebSocketHandlerFunction> functions;

  public ReactiveWebSocketHandlerRouteResolver(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
      final ReactiveFlowControlChain reactiveFlowControlChain,
      final List<ReactiveWebSocketHandlerFunction> functions) {

    this.eventManagerFactory = eventManagerFactory;
    this.sessionRegistry = sessionRegistry;
    this.reactiveHeartbeatFlowControlRegistry = reactiveHeartbeatFlowControlRegistry;
    this.reactiveFlowControlChain = reactiveFlowControlChain;
    this.functions = functions;
  }

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
                      reactiveHeartbeatFlowControlRegistry,
                      reactiveFlowControlChain))
              .ifPresent(handlers::add);
        });

    return handlers;
  }
}
