package io.github.elpis.reactive.websockets.context.resource.connection;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

import java.time.Duration;

@MessageEndpoint("/close")
public class CloseResource {
    @OnMessage(value = "/normal", mode = Mode.BROADCAST)
    public void normal(@SessionAttribute final ReactiveWebSocketSession session) {
        Mono.delay(Duration.ofSeconds(5))
                .then(Mono.fromRunnable(session::close))
                .subscribe();
    }

    @OnMessage(value = "/goingAway", mode = Mode.BROADCAST)
    public void goingAway(@SessionAttribute final ReactiveWebSocketSession session) {
        Mono.delay(Duration.ofSeconds(5))
                .then(Mono.fromRunnable(() -> session.close(CloseStatus.GOING_AWAY)))
                .subscribe();
    }
}
