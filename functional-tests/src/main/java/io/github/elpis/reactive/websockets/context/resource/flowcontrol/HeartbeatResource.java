package io.github.elpis.reactive.websockets.context.resource.flowcontrol;

import io.github.elpis.reactive.websockets.web.annotation.Heartbeat;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;

@MessageEndpoint("/connection")
public class HeartbeatResource {
  @OnMessage(value = "/ping")
  @Heartbeat(interval = 1, timeout = 5)
  public void ping() {
    // Empty for test the Ping-Pong (now using Heartbeat)
  }
}
