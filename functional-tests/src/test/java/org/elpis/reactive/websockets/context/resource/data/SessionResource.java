package org.elpis.reactive.websockets.context.resource.data;

import org.elpis.reactive.websockets.config.Mode;
import org.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
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
