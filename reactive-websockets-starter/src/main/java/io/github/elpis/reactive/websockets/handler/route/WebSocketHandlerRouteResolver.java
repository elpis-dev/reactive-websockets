package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WebSocketHandlerRouteResolver {
  private final WebSocketEventManagerFactory eventManagerFactory;
  private final WebSocketSessionRegistry sessionRegistry;
  private final RateLimiterService rateLimiterService;
  private final List<WebSocketHandlerFunction> functions;

  public WebSocketHandlerRouteResolver(
      final WebSocketEventManagerFactory eventManagerFactory,
      final WebSocketSessionRegistry sessionRegistry,
      final RateLimiterService rateLimiterService,
      final List<WebSocketHandlerFunction> functions) {

    this.eventManagerFactory = eventManagerFactory;
    this.sessionRegistry = sessionRegistry;
    this.rateLimiterService = rateLimiterService;
    this.functions = functions;
  }

  public List<BaseWebSocketHandler> resolve() {
    final List<BaseWebSocketHandler> handlers = new ArrayList<>();

    functions.forEach(
        webSocketHandlerFunction -> {
          WebSocketHandlerFunctions.DefaultRouterFunction function =
              (WebSocketHandlerFunctions.DefaultRouterFunction) webSocketHandlerFunction;
          while (function.getNext() != null) {
            final BaseWebSocketHandler webSocketHandler =
                function.register(eventManagerFactory, sessionRegistry, rateLimiterService);
            if (webSocketHandler == null) {
              break;
            }

            handlers.add(webSocketHandler);
            function = (WebSocketHandlerFunctions.DefaultRouterFunction) function.getNext();
          }

          Optional.ofNullable(
                  function.register(eventManagerFactory, sessionRegistry, rateLimiterService))
              .ifPresent(handlers::add);
        });

    return handlers;
  }
}
