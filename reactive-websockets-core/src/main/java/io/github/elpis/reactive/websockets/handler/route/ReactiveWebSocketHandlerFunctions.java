package io.github.elpis.reactive.websockets.handler.route;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.AdaptiveReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.handler.flowcontrol.ReactiveFlowControlChain;
import io.github.elpis.reactive.websockets.handler.flowcontrol.registry.ReactiveHeartbeatFlowControlRegistry;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
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

/**
 * Factory class for creating ReactiveWebSocketHandlerFunction instances that route WebSocket
 * messages based on specified paths and handler functions.
 *
 * <p>This class provides static methods to create handler functions that can process incoming
 * WebSocket messages using user-defined logic. It supports both message-returning handlers and void
 * handlers.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class ReactiveWebSocketHandlerFunctions {
  private ReactiveWebSocketHandlerFunctions() {}

  /**
   * Creates a ReactiveWebSocketHandlerFunction that routes messages to the specified handler
   * function for the given path.
   *
   * @param path the WebSocket endpoint path
   * @param handlerFunction the function that processes incoming WebSocket messages
   * @param <T> the type of messages returned by the handler function
   * @return a ReactiveWebSocketHandlerFunction that routes messages to the handler function
   */
  public static <T> ReactiveWebSocketHandlerFunction handle(
      final String path, final WebSocketMessageHandlerFunction<T> handlerFunction) {

    return new HandleRouterFunctionReactive<>(path, handlerFunction);
  }

  /**
   * Creates a ReactiveWebSocketHandlerFunction that routes messages to the specified void handler
   * function for the given path.
   *
   * @param path the WebSocket endpoint path
   * @param handlerFunction the void function that processes incoming WebSocket messages
   * @return a ReactiveWebSocketHandlerFunction that routes messages to the void handler function
   */
  public static ReactiveWebSocketHandlerFunction handle(
      final String path, final WebSocketVoidHandlerFunction handlerFunction) {

    return new VoidRouterFunctionReactive(path, handlerFunction);
  }

  /**
   * Creates an empty ReactiveWebSocketHandlerFunction that does not handle any messages.
   *
   * @return an empty ReactiveWebSocketHandlerFunction
   */
  public static ReactiveWebSocketHandlerFunction empty() {
    return new DefaultRouterFunctionReactive(null) {
      @Override
      public BaseReactiveWebSocketHandler register(
          final ReactiveWebSocketEventManagerFactory eventManagerFactory,
          final ReactiveWebSocketSessionRegistry sessionRegistry,
          final JsonMapper jsonMapper,
          final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
          final ReactiveFlowControlChain reactiveFlowControlChain) {
        return null;
      }
    };
  }

  abstract static class DefaultRouterFunctionReactive implements ReactiveWebSocketHandlerFunction {
    final String path;

    ReactiveWebSocketHandlerFunction next = null;

    private DefaultRouterFunctionReactive(final String path) {
      this.path = path;
    }

    ReactiveWebSocketHandlerFunction getNext() {
      return next;
    }

    DefaultRouterFunctionReactive setNext(final ReactiveWebSocketHandlerFunction next) {
      this.next = next;
      return this;
    }
  }

  private static final class HandleRouterFunctionReactive<U> extends DefaultRouterFunctionReactive {
    private static final Logger log = LoggerFactory.getLogger(HandleRouterFunctionReactive.class);
    private final WebSocketMessageHandlerFunction<U> handlerFunction;

    private HandleRouterFunctionReactive(
        final String path, final WebSocketMessageHandlerFunction<U> handlerFunction) {

      super(path);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseReactiveWebSocketHandler register(
        final ReactiveWebSocketEventManagerFactory eventManagerFactory,
        final ReactiveWebSocketSessionRegistry sessionRegistry,
        final JsonMapper jsonMapper,
        final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
        final ReactiveFlowControlChain reactiveFlowControlChain) {

      final WebSocketMessageHandlerFunction<U> handler = this.handlerFunction;

      return new AdaptiveReactiveWebSocketHandler(
          eventManagerFactory,
          sessionRegistry,
          jsonMapper,
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
        final String path, final WebSocketVoidHandlerFunction handlerFunction) {

      super(path);
      this.handlerFunction = handlerFunction;
    }

    @Override
    public BaseReactiveWebSocketHandler register(
        final ReactiveWebSocketEventManagerFactory eventManagerFactory,
        final ReactiveWebSocketSessionRegistry sessionRegistry,
        final JsonMapper jsonMapper,
        final ReactiveHeartbeatFlowControlRegistry reactiveHeartbeatFlowControlRegistry,
        final ReactiveFlowControlChain reactiveFlowControlChain) {

      final WebSocketVoidHandlerFunction handler = this.handlerFunction;

      return new AdaptiveReactiveWebSocketHandler(
          eventManagerFactory,
          sessionRegistry,
          jsonMapper,
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
