package io.github.elpis.reactive.websockets.context.resource.template;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import reactor.core.publisher.Mono;

@MessageEndpoint("/template/error")
public class ErrorReceiverResource {
  private final ReactiveWebSocketTemplate template;

  public ErrorReceiverResource(final ReactiveWebSocketTemplate template) {
    this.template = template;
  }

  @OnMessage("/session")
  public Publisher<?> sessionError(
      @SessionAttribute final ReactiveWebSocketSession session,
      @RequestParam(value = "userName") final String userName) {

    return template.sendError(
        "/template/error/session",
        session.getSessionId(),
        "%s missing 'read' scope".formatted(userName));
  }

  @OnMessage("/broadcast")
  public Publisher<?> broadcastError() {
    return Mono.empty();
  }
}
