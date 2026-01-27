package io.github.elpis.reactive.websockets.context.resource.registry;

import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@MessageEndpoint("/registry")
public class WebSocketRegistryResource {

  @OnMessage("/count")
  public Publisher<?> count() {
    return Mono.never();
  }

  @OnMessage("/totalCount/one")
  public Publisher<?> totalCountOne() {
    return Mono.never();
  }

  @OnMessage("/totalCount/two")
  public Publisher<?> totalCountTwo() {
    return Mono.never();
  }

  @OnMessage("/active/one")
  public Publisher<?> activeOne() {
    return Mono.never();
  }

  @OnMessage("/active/two")
  public Publisher<?> activeTwo() {
    return Mono.never();
  }

  @OnMessage("/shutdown")
  public Publisher<?> shutdown() {
    return Mono.never();
  }
}
