package org.elpis.reactive.websockets.handler.route;

import org.elpis.reactive.websockets.config.Mode;
import org.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import org.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.session.WebSocketSessionRegistry;

@FunctionalInterface
public interface WebSocketHandlerFunction {
    BaseWebSocketHandler register(final WebSocketEventManagerFactory eventManagerFactory,
                                  final WebSocketSessionRegistry sessionRegistry);

    default <T> WebSocketHandlerFunction handle(final String path,
                                                final Mode mode,
                                                final boolean pingEnabled,
                                                final long pingInterval,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction =
                (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                        .handle(path, mode, pingEnabled, pingInterval, function);

        return webSocketHandlerFunction.setNext(this);
    }

    default WebSocketHandlerFunction handle(final String path,
                                            final Mode mode,
                                            final boolean pingEnabled,
                                            final long pingInterval,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction =
                (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                        .handle(path, mode, pingEnabled, pingInterval, function);

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
