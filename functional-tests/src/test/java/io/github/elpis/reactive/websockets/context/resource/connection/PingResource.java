package io.github.elpis.reactive.websockets.context.resource.connection;

import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;

@MessageEndpoint("/connection")
public class PingResource {
  @OnMessage(
      value = "/ping",
      mode = Mode.BROADCAST,
      heartbeat = @Heartbeat(interval = 1, timeout = 5))
  public void ping() {
    // Empty for test the Ping-Pong (now using Heartbeat)
  }
}
