package io.github.elpis.reactive.websockets.context.resource.impl;

import io.github.elpis.reactive.websockets.event.manager.ReactiveWebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseReactiveWebSocketHandler;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSessionRegistry;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class TestWebSocketHandler extends BaseReactiveWebSocketHandler {
  private final List<String> history =
      List.of("Alice: Hello, everyone!", "Bob: Hi, Alice!", "Charlie: Good morning!");

  /**
   * Creates a BaseWebSocketHandler with minimal dependencies.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   */
  protected TestWebSocketHandler(
      final ReactiveWebSocketEventManagerFactory eventManagerFactory,
      final ReactiveWebSocketSessionRegistry sessionRegistry,
      final JsonMapper jsonMapper) {
    super(eventManagerFactory, sessionRegistry, jsonMapper, "/test/handler");
  }

  @Override
  protected Publisher<?> processMessages(
      final WebSocketSessionContext context,
      final Flux<WebSocketMessage> __,
      final Sinks.Many<Object> outboundSink) {
    return Flux.fromIterable(history).doOnNext(outboundSink::tryEmitNext);
  }
}
