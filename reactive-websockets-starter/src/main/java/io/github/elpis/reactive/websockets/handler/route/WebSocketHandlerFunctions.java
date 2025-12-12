package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.BroadcastWebSocketResourceHandler;
import io.github.elpis.reactive.websockets.handler.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.handler.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.handler.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

public final class WebSocketHandlerFunctions {
  private WebSocketHandlerFunctions() {}

  public static <T> WebSocketHandlerFunction handle(
      final String path,
      final Mode mode,
      final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return handle(path, mode, false, 30L, 60L, handlerFunction);
  }

  public static <T> WebSocketHandlerFunction handle(
      final String path,
      final Mode mode,
      final boolean heartbeatEnabled,
      final long heartbeatInterval,
      final long heartbeatTimeout,
      final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return new HandleRouterFunction<>(
        path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, mode, handlerFunction);
  }

  public static WebSocketHandlerFunction handle(
      final String path, final Mode mode, final WebSocketVoidHandlerFunction handlerFunction) {

    return handle(path, mode, false, 30L, 60L, handlerFunction);
  }

  public static WebSocketHandlerFunction handle(
      final String path,
      final Mode mode,
      final boolean heartbeatEnabled,
      final long heartbeatInterval,
      final long heartbeatTimeout,
      final WebSocketVoidHandlerFunction handlerFunction) {

    return new VoidRouterFunction(
        path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, mode, handlerFunction);
  }

  public static WebSocketHandlerFunction empty() {
    return new DefaultRouterFunction(null, false, -1L, -1L, null) {
      @Override
      public BaseWebSocketHandler register(
          final WebSocketEventManagerFactory eventManagerFactory,
          final WebSocketSessionRegistry sessionRegistry,
          final RateLimiterService rateLimiterService) {
        return null;
      }
    };
  }

  abstract static class DefaultRouterFunction implements WebSocketHandlerFunction {
    final String path;
    final boolean heartbeatEnabled;
    final long heartbeatInterval;
    final long heartbeatTimeout;
    final Mode mode;

    WebSocketHandlerFunction next = null;

    private DefaultRouterFunction(
        String path,
        boolean heartbeatEnabled,
        long heartbeatInterval,
        long heartbeatTimeout,
        Mode mode) {

      this.path = path;
      this.heartbeatEnabled = heartbeatEnabled;
      this.heartbeatInterval = heartbeatInterval;
      this.heartbeatTimeout = heartbeatTimeout;
      this.mode = mode;
    }

    WebSocketHandlerFunction getNext() {
      return next;
    }

    DefaultRouterFunction setNext(WebSocketHandlerFunction next) {
      this.next = next;
      return this;
    }
  }

  private static final class HandleRouterFunction<U> extends DefaultRouterFunction {
    private final WebSocketMessageHandlerFunction<U> handlerFunction;

    private HandleRouterFunction(
        String path,
        boolean heartbeatEnabled,
        long heartbeatInterval,
        long heartbeatTimeout,
        Mode mode,
        WebSocketMessageHandlerFunction<U> handlerFunction) {

      super(path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, mode);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseWebSocketHandler register(
        final WebSocketEventManagerFactory eventManagerFactory,
        final WebSocketSessionRegistry sessionRegistry,
        final RateLimiterService rateLimiterService) {

      return switch (this.mode) {
        case BROADCAST ->
            new BroadcastWebSocketResourceHandler(
                eventManagerFactory,
                sessionRegistry,
                rateLimiterService,
                path,
                HeartbeatConfig.of(heartbeatInterval, heartbeatTimeout),
                RateLimitConfig.disabled(),
                BackpressureConfig.disabled()) {
              @Override
              public Publisher<?> apply(
                  WebSocketSessionContext context, Flux<WebSocketMessage> messages) {
                return handlerFunction.apply(context, messages);
              }
            };
        case SESSION -> null;
      };
    }
  }

  private static final class VoidRouterFunction extends DefaultRouterFunction {
    private final WebSocketVoidHandlerFunction handlerFunction;

    private VoidRouterFunction(
        String path,
        boolean heartbeatEnabled,
        long heartbeatInterval,
        long heartbeatTimeout,
        Mode mode,
        WebSocketVoidHandlerFunction handlerFunction) {

      super(path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, mode);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseWebSocketHandler register(
        final WebSocketEventManagerFactory eventManagerFactory,
        final WebSocketSessionRegistry sessionRegistry,
        final RateLimiterService rateLimiterService) {
      return switch (this.mode) {
        case BROADCAST ->
            new BroadcastWebSocketResourceHandler(
                eventManagerFactory,
                sessionRegistry,
                rateLimiterService,
                path,
                HeartbeatConfig.of(heartbeatInterval, heartbeatTimeout),
                RateLimitConfig.disabled(),
                BackpressureConfig.disabled()) {
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
  public interface WebSocketMessageHandlerFunction<T>
      extends BiFunction<WebSocketSessionContext, Flux<WebSocketMessage>, Publisher<T>> {}

  @FunctionalInterface
  public interface WebSocketVoidHandlerFunction
      extends BiConsumer<WebSocketSessionContext, Flux<WebSocketMessage>> {}
}
