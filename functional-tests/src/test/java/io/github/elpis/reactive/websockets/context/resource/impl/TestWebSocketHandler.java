package io.github.elpis.reactive.websockets.context.resource.impl;

import io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory;
import io.github.elpis.reactive.websockets.handler.BaseWebSocketHandler;
import io.github.elpis.reactive.websockets.session.SessionStreams;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class TestWebSocketHandler extends BaseWebSocketHandler {
  private final List<String> history =
      List.of("Alice: Hello, everyone!", "Bob: Hi, Alice!", "Charlie: Good morning!");

  /**
   * Creates a BaseWebSocketHandler with minimal dependencies.
   *
   * @param eventManagerFactory factory for creating event managers
   * @param sessionRegistry registry for session management
   */
  protected TestWebSocketHandler(
      WebSocketEventManagerFactory eventManagerFactory, WebSocketSessionRegistry sessionRegistry) {
    super(eventManagerFactory, sessionRegistry, "/test/handler");
  }

  @Override
  protected Publisher<?> processMessages(
      final WebSocketSessionContext context, final SessionStreams streams) {
    return Flux.fromIterable(history).doOnNext(entry -> streams.outboundSink().tryEmitNext(entry));
  }
}
