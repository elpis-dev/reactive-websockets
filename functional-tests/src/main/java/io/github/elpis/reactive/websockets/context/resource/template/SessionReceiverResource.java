package io.github.elpis.reactive.websockets.context.resource.template;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@MessageEndpoint("/template/session")
public class SessionReceiverResource {
  private final List<String> patterns = List.of("%s is good", "%s is awesome");

  private final ReactiveWebSocketTemplate template;

  public SessionReceiverResource(final ReactiveWebSocketTemplate template) {
    this.template = template;
  }

  @OnMessage("/single")
  public Publisher<?> sendSession(
      @SessionAttribute final ReactiveWebSocketSession session,
      @RequestParam(value = "userName") final String userName) {

    return template.sendToSession(
        "/template/session/single", session.getSessionId(), "%s connected".formatted(userName));
  }

  @OnMessage("/many")
  public Publisher<?> sendStreamToSession(
      @SessionAttribute final ReactiveWebSocketSession session,
      @RequestParam(value = "userName") final String userName) {

    return template.sendToSession(
        "/template/session/many",
        session.getSessionId(),
        Flux.fromIterable(patterns).map(pattern -> pattern.formatted(userName)));
  }

  @OnMessage("/multi/single")
  public Publisher<?> sendSessions() {
    return Mono.never();
  }

  @OnMessage("/multi/many")
  public Publisher<?> sendStreamToSessions() {
    return Mono.never();
  }
}
