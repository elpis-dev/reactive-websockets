package io.github.elpis.reactive.websockets.flowcontrol;

import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.util.TriFunction;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

public interface FlowControlPolicy
    extends TriFunction<
        String, WebSocketSessionContext, Flux<WebSocketMessage>, Flux<WebSocketMessage>> {
  FlowControlPlacement placement();
}
