package org.elpis.reactive.websockets.config.handler.route;

import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;

@FunctionalInterface
public interface WebSocketHandlerFunction {
    BaseWebSocketHandler register(final WebSessionRegistry registry);

    default <T> WebSocketHandlerFunction handle(final String path,
                                                final Mode mode,
                                                final boolean pingPongEnabled,
                                                final long pingPongInterval,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, pingPongEnabled, pingPongInterval, function);

        webSocketHandlerFunction.setNext(this);

        return webSocketHandlerFunction;
    }

    default WebSocketHandlerFunction handle(final String path,
                                            final Mode mode,
                                            final boolean pingPongEnabled,
                                            final long pingPongInterval,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        final WebSocketHandlerFunctions.DefaultRouterFunction webSocketHandlerFunction = (WebSocketHandlerFunctions.DefaultRouterFunction) WebSocketHandlerFunctions
                .handle(path, mode, pingPongEnabled, pingPongInterval, function);

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
