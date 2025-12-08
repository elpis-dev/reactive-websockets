package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import io.github.elpis.reactive.websockets.web.annotation.SocketController;
import io.github.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import reactor.core.publisher.Mono;

import java.util.Optional;

@SocketController("/session")
public class SessionResource {

    @SocketMapping(value = "/nonRequired", mode = Mode.SHARED)
    public Mono<String> nonRequired(@SessionAttribute(required = false) final ReactiveWebSocketSession session) {
        return Mono.just(session.getSessionId());
    }

    @SocketMapping(value = "/required", mode = Mode.SHARED)
    public Mono<String> required(@SessionAttribute final ReactiveWebSocketSession session) {
        return Mono.just(session.getSessionId());
    }

    @SocketMapping(value = "/optional", mode = Mode.SHARED)
    public Mono<String> optional(@SessionAttribute final Optional<ReactiveWebSocketSession> session) {
        return Mono.justOrEmpty(session)
                .map(ReactiveWebSocketSession::getSessionId);
    }
}
