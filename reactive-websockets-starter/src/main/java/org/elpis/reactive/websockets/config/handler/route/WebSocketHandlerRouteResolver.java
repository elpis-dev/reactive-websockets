package org.elpis.reactive.websockets.config.handler.route;

import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.config.handler.WebSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class WebSocketHandlerRouteResolver {
    private final List<WebSocketHandlerFunction> functions;
    private final WebSessionRegistry registry;

    @Autowired
    public WebSocketHandlerRouteResolver(final List<WebSocketHandlerFunction> functions,
                                         final WebSessionRegistry registry) {

        this.functions = functions;
        this.registry = registry;
    }

    public List<BaseWebSocketHandler> resolve() {
        final List<BaseWebSocketHandler> handlers = new ArrayList<>();

        functions.forEach(webSocketHandlerFunction -> {
            WebSocketHandlerFunctions.DefaultRouterFunction function = (WebSocketHandlerFunctions.DefaultRouterFunction) webSocketHandlerFunction;
            while (function.getNext() != null) {
                final BaseWebSocketHandler webSocketHandler = function.register(registry);
                if (webSocketHandler == null) {
                    break;
                }

                handlers.add(webSocketHandler);
                function = (WebSocketHandlerFunctions.DefaultRouterFunction) function.getNext();
            }

            Optional.ofNullable(function.register(registry))
                    .ifPresent(handlers::add);
        });

        return handlers;
    }
}
