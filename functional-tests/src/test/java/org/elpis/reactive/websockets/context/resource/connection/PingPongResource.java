package org.elpis.reactive.websockets.context.resource.connection;

import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.web.annotation.PingPong;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Flux;

import java.time.Duration;

@SocketController("/connection")
public class PingPongResource {
    @SocketMapping(value = "/ping", mode = Mode.SHARED, pingPong = @PingPong)
    public void ping() {
    }
}
