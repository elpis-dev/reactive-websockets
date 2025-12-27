package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.AdaptiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.config.BackpressureConfig;
import io.github.elpis.reactive.websockets.handler.config.HeartbeatConfig;
import io.github.elpis.reactive.websockets.handler.config.RateLimitConfig;
import io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class WebSocketHandlerFunctions {
  private WebSocketHandlerFunctions() {}

  public static <T> WebSocketHandlerFunction handle(
      final String path, final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return handle(path, false, 30L, 60L, handlerFunction);
  }

  public static <T> WebSocketHandlerFunction handle(
      final String path,
      final boolean heartbeatEnabled,
      final long heartbeatInterval,
      final long heartbeatTimeout,
      final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return new HandleRouterFunction<>(
        path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, handlerFunction);
  }

  public static WebSocketHandlerFunction handle(
      final String path, final WebSocketVoidHandlerFunction handlerFunction) {

    return handle(path, false, 30L, 60L, handlerFunction);
  }

  public static WebSocketHandlerFunction handle(
      final String path,
      final boolean heartbeatEnabled,
      final long heartbeatInterval,
      final long heartbeatTimeout,
      final WebSocketVoidHandlerFunction handlerFunction) {

    return new VoidRouterFunction(
        path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, handlerFunction);
  }

  public static WebSocketHandlerFunction empty() {
    return new DefaultRouterFunction(null, false, -1L, -1L) {
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

    WebSocketHandlerFunction next = null;

    private DefaultRouterFunction(
        String path, boolean heartbeatEnabled, long heartbeatInterval, long heartbeatTimeout) {

      this.path = path;
      this.heartbeatEnabled = heartbeatEnabled;
      this.heartbeatInterval = heartbeatInterval;
      this.heartbeatTimeout = heartbeatTimeout;
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
    private static final Logger log = LoggerFactory.getLogger(HandleRouterFunction.class);
    private final WebSocketMessageHandlerFunction<U> handlerFunction;

    private HandleRouterFunction(
        String path,
        boolean heartbeatEnabled,
        long heartbeatInterval,
        long heartbeatTimeout,
        WebSocketMessageHandlerFunction<U> handlerFunction) {

      super(path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseWebSocketHandler register(
        final WebSocketEventManagerFactory eventManagerFactory,
        final WebSocketSessionRegistry sessionRegistry,
        final RateLimiterService rateLimiterService) {

      final WebSocketMessageHandlerFunction<U> handler = this.handlerFunction;

      return new AdaptiveWebSocketHandler(
          eventManagerFactory,
          sessionRegistry,
          path,
          HeartbeatConfig.of(heartbeatInterval, heartbeatTimeout),
          RateLimitConfig.disabled(),
          BackpressureConfig.disabled(),
          rateLimiterService) {

        @Override
        protected Publisher<?> processMessages(
            WebSocketSessionContext context, SessionStreams streams) {

          return streams
              .inboundFlux()
              .transform(flux -> handler.apply(context, flux))
              .doOnNext(msg -> streams.outboundSink().tryEmitNext(msg))
              .onErrorResume(
                  e -> {
                    log.error("Error in handler function: {}", e.getMessage(), e);
                    return Mono.never();
                  });
        }
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
        WebSocketVoidHandlerFunction handlerFunction) {

      super(path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseWebSocketHandler register(
        final WebSocketEventManagerFactory eventManagerFactory,
        final WebSocketSessionRegistry sessionRegistry,
        final RateLimiterService rateLimiterService) {

      final WebSocketVoidHandlerFunction handler = this.handlerFunction;

      return new AdaptiveWebSocketHandler(
          eventManagerFactory,
          sessionRegistry,
          path,
          HeartbeatConfig.of(heartbeatInterval, heartbeatTimeout),
          RateLimitConfig.disabled(),
          BackpressureConfig.disabled(),
          rateLimiterService) {

        @Override
        protected Publisher<?> processMessages(
            WebSocketSessionContext context, SessionStreams streams) {

          final Flux<WebSocketMessage> messages = streams.inboundFlux();

          handler.accept(context, messages);
          return Mono.never();
        }
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
