package org.elpis.reactive.websockets.config.handler.route;

import org.elpis.reactive.websockets.config.handler.BaseWebSocketHandler;
import org.elpis.reactive.websockets.config.handler.BroadcastWebSocketResourceHandler;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.config.handler.WebSessionRegistry;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class WebSocketHandlerFunctions {
    private WebSocketHandlerFunctions() {
    }

    public static <T> WebSocketHandlerFunction handle(final String path,
                                                      final Mode mode,
                                                      final WebSocketMessageHandlerFunction<T> handlerFunction) {

        return handle(path, mode, false, 0L, handlerFunction);
    }

    public static <T> WebSocketHandlerFunction handle(final String path,
                                                      final Mode mode,
                                                      final boolean pingEnabled,
                                                      final long pingInterval,
                                                      final WebSocketMessageHandlerFunction<T> handlerFunction) {

        return new HandleRouterFunction<>(path, pingEnabled, pingInterval, mode, handlerFunction);
    }

    public static WebSocketHandlerFunction handle(final String path,
                                                  final Mode mode,
                                                  final WebSocketVoidHandlerFunction handlerFunction) {

        return handle(path, mode, false, 0L, handlerFunction);
    }

    public static WebSocketHandlerFunction handle(final String path,
                                                  final Mode mode,
                                                  final boolean pingEnabled,
                                                  final long pingInterval,
                                                  final WebSocketVoidHandlerFunction handlerFunction) {

        return new VoidRouterFunction(path, pingEnabled, pingInterval, mode, handlerFunction);
    }

    public static WebSocketHandlerFunction empty() {
        return registry -> null;
    }

    abstract static class DefaultRouterFunction implements WebSocketHandlerFunction {
        final String path;
        final boolean pingEnabled;
        final long pingInterval;
        final Mode mode;

        WebSocketHandlerFunction next = null;

        private DefaultRouterFunction(String path,
                                      boolean pingEnabled,
                                      long pingInterval,
                                      Mode mode) {

            this.path = path;
            this.pingEnabled = pingEnabled;
            this.pingInterval = pingInterval;
            this.mode = mode;
        }

        WebSocketHandlerFunction getNext() {
            return next;
        }

        void setNext(WebSocketHandlerFunction next) {
            this.next = next;
        }
    }

    private static final class HandleRouterFunction<U> extends DefaultRouterFunction {
        private final WebSocketMessageHandlerFunction<U> handlerFunction;

        private HandleRouterFunction(String path,
                                     boolean pingEnabled,
                                     long pingInterval,
                                     Mode mode,
                                     WebSocketMessageHandlerFunction<U> handlerFunction) {

            super(path, pingEnabled, pingInterval, mode);
            this.handlerFunction = handlerFunction;
        }

        @Override
        public BaseWebSocketHandler register(final WebSessionRegistry registry) {
            return switch (this.mode) {
                case SHARED ->
                        new BroadcastWebSocketResourceHandler(registry, path, pingEnabled, pingInterval) {
                            @Override
                            public Publisher<?> apply(WebSocketSessionContext context, Flux<WebSocketMessage> messages) {
                                return handlerFunction.apply(context, messages);
                            }
                        };
                case SESSION -> null;
            };
        }
    }

    private static final class VoidRouterFunction extends DefaultRouterFunction {
        private final WebSocketVoidHandlerFunction handlerFunction;

        private VoidRouterFunction(String path,
                                   boolean pingEnabled,
                                   long pingInterval,
                                   Mode mode,
                                   WebSocketVoidHandlerFunction handlerFunction) {

            super(path, pingEnabled, pingInterval, mode);
            this.handlerFunction = handlerFunction;
        }

        @Override
        public BaseWebSocketHandler register(final WebSessionRegistry registry) {
            return switch (this.mode) {
                case SHARED ->
                        new BroadcastWebSocketResourceHandler(registry, path, pingEnabled, pingInterval) {
                            @Override
                            public void run(WebSocketSessionContext context, Flux<WebSocketMessage> messages) {
                                handlerFunction.accept(context, messages);
                            }
                        };
                case SESSION -> null;
            };
        }
    }

    @FunctionalInterface
    public interface WebSocketMessageHandlerFunction<T> extends BiFunction<WebSocketSessionContext, Flux<WebSocketMessage>, Publisher<T>> {
    }

    @FunctionalInterface
    public interface WebSocketVoidHandlerFunction extends BiConsumer<WebSocketSessionContext, Flux<WebSocketMessage>> {

    }

}
