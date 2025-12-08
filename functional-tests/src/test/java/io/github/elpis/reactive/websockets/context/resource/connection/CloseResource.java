package io.github.elpis.reactive.websockets.context.resource.connection;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.web.annotation.SocketController;
import io.github.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SocketController("/close")
public class CloseResource {
    @SocketMapping(value = "/normal", mode = Mode.SHARED)
    public void normal(@SessionAttribute final ReactiveWebSocketSession session) {
        Mono.delay(Duration.ofSeconds(5))
                .then(Mono.fromRunnable(session::close))
                .subscribe();
    }

    @SocketMapping(value = "/goingAway", mode = Mode.SHARED)
    public void goingAway(@SessionAttribute final ReactiveWebSocketSession session) {
        Mono.delay(Duration.ofSeconds(5))
                .then(Mono.fromRunnable(() -> session.close(CloseStatus.GOING_AWAY)))
                .subscribe();
    }
}
