package org.elpis.reactive.websockets.context.resource.connection;

import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.web.annotation.PingPong;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;

@SocketController("/connection")
public class PingPongResource {
    @SocketMapping(value = "/ping", mode = Mode.SHARED, pingPong = @PingPong)
    public void ping() {
        //Empty for test the Ping-Pong
    }
}
