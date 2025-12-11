package io.github.elpis.reactive.websockets.context;

import io.github.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import io.github.elpis.reactive.websockets.EnableReactiveSockets;
import io.github.elpis.reactive.websockets.event.matcher.impl.ClosedSessionEventSelectorMatcher;
import io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableReactiveSockets
@EnableReactiveSocketSecurity
@Import({
  // Event Matchers
  ClosedSessionEventSelectorMatcher.class,
  WebSocketSessionRegistry.class
})
public class BootStarter {
  public static void main(String[] args) {
    SpringApplication.run(BootStarter.class, args);
  }

  public enum Example {
    VOID
  }
}
