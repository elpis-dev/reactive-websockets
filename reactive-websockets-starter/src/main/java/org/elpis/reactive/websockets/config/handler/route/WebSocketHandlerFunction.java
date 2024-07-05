package org.elpis.reactive.websockets.config.handler.route;

import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.config.handler.WebSessionRegistry;

@FunctionalInterface
public interface WebSocketHandlerFunction {
    BaseWebSocketHandler register(final WebSessionRegistry registry);

    default <T> WebSocketHandlerFunction handle(final String path,
                                                final Mode mode,
                                                final boolean pingEnabled,
                                                final long pingInterval,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, pingEnabled, pingInterval, function);

        webSocketHandlerFunction.setNext(this);

        return webSocketHandlerFunction;
    }

    default WebSocketHandlerFunction handle(final String path,
                                            final Mode mode,
                                            final boolean pingEnabled,
                                            final long pingInterval,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, pingEnabled, pingInterval, function);

        webSocketHandlerFunction.setNext(this);

        return webSocketHandlerFunction;
    }

    default <T> WebSocketHandlerFunction handle(final String path, final Mode mode,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, function);

        webSocketHandlerFunction.setNext(this);

        return webSocketHandlerFunction;
    }

    default WebSocketHandlerFunction handle(final String path, final Mode mode,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, function);

        webSocketHandlerFunction.setNext(this);

        return webSocketHandlerFunction;
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
