package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.AdaptiveReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

// TODO: Check if it's consistent with the latest API changes
public final class ReactiveWebSocketHandlerFunctions {
  private ReactiveWebSocketHandlerFunctions() {}

  public static <T> ReactiveWebSocketHandlerFunction handle(
      final String path, final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return handle(path, false, 30L, 60L, handlerFunction);
  }

  public static <T> ReactiveWebSocketHandlerFunction handle(
      final String path,
      final boolean heartbeatEnabled,
      final long heartbeatInterval,
      final long heartbeatTimeout,
      final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return new HandleRouterFunctionReactive<>(
        path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, handlerFunction);
  }

  public static ReactiveWebSocketHandlerFunction handle(
      final String path, final WebSocketVoidHandlerFunction handlerFunction) {

    return handle(path, false, 30L, 60L, handlerFunction);
  }

  public static ReactiveWebSocketHandlerFunction handle(
      final String path,
      final boolean heartbeatEnabled,
      final long heartbeatInterval,
      final long heartbeatTimeout,
      final WebSocketVoidHandlerFunction handlerFunction) {

    return new VoidRouterFunctionReactive(
        path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout, handlerFunction);
  }

  public static ReactiveWebSocketHandlerFunction empty() {
    return new DefaultRouterFunctionReactive(null, false, -1L, -1L) {
      @Override
      public BaseReactiveWebSocketHandler register(
          final ReactiveWebSocketEventManagerFactory eventManagerFactory,
          final ReactiveWebSocketSessionRegistry sessionRegistry,
          final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
          final ReactiveFlowControlChain reactiveFlowControlChain) {
        return null;
      }
    };
  }

  abstract static class DefaultRouterFunctionReactive implements ReactiveWebSocketHandlerFunction {
    final String path;
    final boolean heartbeatEnabled;
    final long heartbeatInterval;
    final long heartbeatTimeout;

    ReactiveWebSocketHandlerFunction next = null;

    private DefaultRouterFunctionReactive(
        String path, boolean heartbeatEnabled, long heartbeatInterval, long heartbeatTimeout) {

      this.path = path;
      this.heartbeatEnabled = heartbeatEnabled;
      this.heartbeatInterval = heartbeatInterval;
      this.heartbeatTimeout = heartbeatTimeout;
    }

    ReactiveWebSocketHandlerFunction getNext() {
      return next;
    }

    DefaultRouterFunctionReactive setNext(ReactiveWebSocketHandlerFunction next) {
      this.next = next;
      return this;
    }
  }

  private static final class HandleRouterFunctionReactive<U> extends DefaultRouterFunctionReactive {
    private static final Logger log = LoggerFactory.getLogger(HandleRouterFunctionReactive.class);
    private final WebSocketMessageHandlerFunction<U> handlerFunction;

    private HandleRouterFunctionReactive(
        String path,
        boolean heartbeatEnabled,
        long heartbeatInterval,
        long heartbeatTimeout,
        WebSocketMessageHandlerFunction<U> handlerFunction) {

      super(path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseReactiveWebSocketHandler register(
        final ReactiveWebSocketEventManagerFactory eventManagerFactory,
        final ReactiveWebSocketSessionRegistry sessionRegistry,
        final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
        final ReactiveFlowControlChain reactiveFlowControlChain) {

      final WebSocketMessageHandlerFunction<U> handler = this.handlerFunction;

      return new AdaptiveReactiveWebSocketHandler(
          eventManagerFactory,
          sessionRegistry,
          path,
          reactiveHeartbeatFlowControlRegistry,
          reactiveFlowControlChain) {

        @Override
        protected Publisher<?> processMessages(
            final WebSocketSessionContext context,
            final Flux<WebSocketMessage> inboundStream,
            final Sinks.Many<Object> outboundSink) {

          return inboundStream
              .transform(flux -> handler.apply(context, flux))
              .doOnNext(outboundSink::tryEmitNext)
              .onErrorResume(
                  e -> {
                    log.error("Error in handler function: {}", e.getMessage(), e);
                    return Mono.never();
                  });
        }
      };
    }
  }

  private static final class VoidRouterFunctionReactive extends DefaultRouterFunctionReactive {
    private final WebSocketVoidHandlerFunction handlerFunction;

    private VoidRouterFunctionReactive(
        String path,
        boolean heartbeatEnabled,
        long heartbeatInterval,
        long heartbeatTimeout,
        WebSocketVoidHandlerFunction handlerFunction) {

      super(path, heartbeatEnabled, heartbeatInterval, heartbeatTimeout);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseReactiveWebSocketHandler register(
        final ReactiveWebSocketEventManagerFactory eventManagerFactory,
        final ReactiveWebSocketSessionRegistry sessionRegistry,
        final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
        final ReactiveFlowControlChain reactiveFlowControlChain) {

      final WebSocketVoidHandlerFunction handler = this.handlerFunction;

      return new AdaptiveReactiveWebSocketHandler(
          eventManagerFactory,
          sessionRegistry,
          path,
          reactiveHeartbeatFlowControlRegistry,
          reactiveFlowControlChain) {

        @Override
        protected Publisher<?> processMessages(
            final WebSocketSessionContext context,
            final Flux<WebSocketMessage> inboundStream,
            final Sinks.Many<Object> outboundSink) {

          handler.accept(context, inboundStream);
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
