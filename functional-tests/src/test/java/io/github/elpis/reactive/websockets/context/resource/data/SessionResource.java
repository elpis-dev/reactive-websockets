package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.util.Optional;
import org.springframework.web.bind.annotation.SessionAttribute;
import reactor.core.publisher.Mono;

@MessageEndpoint("/session")
public class SessionResource {

  @OnMessage(value = "/nonRequired")
  public Mono<String> nonRequired(
      @SessionAttribute(required = false) final ReactiveWebSocketSession session) {
    return Mono.just(session.getSessionId());
  }

  @OnMessage(value = "/required")
  public Mono<String> required(@SessionAttribute final ReactiveWebSocketSession session) {
    return Mono.just(session.getSessionId());
  }

  @OnMessage(value = "/optional")
  public Mono<String> optional(@SessionAttribute final Optional<ReactiveWebSocketSession> session) {
    return Mono.justOrEmpty(session).map(ReactiveWebSocketSession::getSessionId);
  }
}
