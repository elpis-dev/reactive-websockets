package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;

@FunctionalInterface
public interface WebSocketHandlerFunction {
    BaseWebSocketHandler register(final WebSocketEventManagerFactory eventManagerFactory,
                                  final WebSocketSessionRegistry sessionRegistry);

    default <T> WebSocketHandlerFunction handle(final String path,
                                                final Mode mode,
                                                final boolean heartbeatEnabled,
                                                final long heartbeatInterval,
                                                final long heartbeatTimeout,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction =
                (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                        .handle(path, mode, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, function);

        return webSocketHandlerFunction.setNext(this);
    }

    default WebSocketHandlerFunction handle(final String path,
                                            final Mode mode,
                                            final boolean heartbeatEnabled,
                                            final long heartbeatInterval,
                                            final long heartbeatTimeout,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction =
                (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                        .handle(path, mode, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, function);

        return webSocketHandlerFunction.setNext(this);
    }

    default <T> WebSocketHandlerFunction handle(final String path, final Mode mode,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, function);

        return webSocketHandlerFunction.setNext(this);
    }

    default WebSocketHandlerFunction handle(final String path, final Mode mode,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, function);

        return webSocketHandlerFunction.setNext(this);
    }

    default WebSocketHandlerFunction and(final WebSocketHandlerFunction another) {
        WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) another;
        while (webSocketHandlerFunction.getNext() != null) {
            webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) webSocketHandlerFunction.getNext();
        }
        webSocketHandlerFunction.setNext(this);

        return another;
    }

}
