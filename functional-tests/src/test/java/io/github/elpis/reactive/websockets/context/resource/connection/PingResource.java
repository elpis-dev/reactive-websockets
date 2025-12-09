package io.github.elpis.reactive.websockets.context.resource.connection;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.Ping;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;

@MessageEndpoint("/connection")
public class PingResource {
    @OnMessage(value = "/ping", mode = Mode.SHARED, ping = @Ping)
    public void ping() {
        //Empty for test the Ping-Pong
    }
}
