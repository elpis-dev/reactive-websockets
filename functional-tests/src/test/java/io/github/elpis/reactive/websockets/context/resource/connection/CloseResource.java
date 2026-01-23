package io.github.elpis.reactive.websockets.context.resource.connection;

import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.time.Duration;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

@MessageEndpoint("/close")
public class CloseResource {
  @OnMessage(value = "/normal")
  public Mono<Void> normal(@SessionAttribute final ReactiveWebSocketSession session) {
    return Mono.delay(Duration.ofSeconds(1)).flatMap(i -> session.close());
  }

  @OnMessage(value = "/goingAway")
  public Mono<Void> goingAway(@SessionAttribute final ReactiveWebSocketSession session) {
    return Mono.delay(Duration.ofSeconds(1)).flatMap(i -> session.close(CloseStatus.GOING_AWAY));
  }
}
