package org.elpis.reactive.websockets.context.resource.connection;

import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.web.annotation.Ping;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;

@SocketController("/connection")
public class PingResource {
    @SocketMapping(value = "/ping", mode = Mode.SHARED, ping = @Ping)
    public void ping() {
        //Empty for test the Ping-Pong
    }
}
