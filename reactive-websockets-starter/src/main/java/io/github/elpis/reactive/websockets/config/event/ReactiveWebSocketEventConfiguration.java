package io.github.elpis.reactive.websockets.config.event;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  ReactiveEventManagerConfiguration.class,
  ClosedConnectionHandlerConfiguration.class,
  SpringSystemEventsConfiguration.class
})
public class ReactiveWebSocketEventConfiguration {}
