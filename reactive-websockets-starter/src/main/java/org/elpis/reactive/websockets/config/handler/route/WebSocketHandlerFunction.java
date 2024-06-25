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

        return new WebSocketHandlerFunctions.CombinedRouterFunction(this,
                WebSocketHandlerFunctions.handle(path, mode, pingPongEnabled, pingPongInterval, function));
    }

    default WebSocketHandlerFunction handle(final String path,
                                            final Mode mode,
                                            final boolean pingPongEnabled,
                                            final long pingPongInterval,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        return new WebSocketHandlerFunctions.CombinedRouterFunction(this,
                WebSocketHandlerFunctions.handle(path, mode, pingPongEnabled, pingPongInterval, function));
    }

    default <T> WebSocketHandlerFunction handle(final String path, final Mode mode,
                                                final WebSocketHandlerFunctions.WebSocketMessageHandlerFunction<T> function) {

        return new WebSocketHandlerFunctions.CombinedRouterFunction(this,
                WebSocketHandlerFunctions.handle(path, mode, function));
    }

    default WebSocketHandlerFunction handle(final String path, final Mode mode,
                                            final WebSocketHandlerFunctions.WebSocketVoidHandlerFunction function) {

        return new WebSocketHandlerFunctions.CombinedRouterFunction(this,
                WebSocketHandlerFunctions.handle(path, mode, function));
    }


}
