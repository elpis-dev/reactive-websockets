package io.github.elpis.reactive.websockets.context.resource.template;

import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@MessageEndpoint("/template/broadcast")
public class BroadcastReceiverResource {

  @OnMessage("/single")
  public Publisher<?> sendBroadcast() {
    return Mono.never();
  }

  @OnMessage("/many")
  public Publisher<?> sendBroadcastMany() {
    return Mono.never();
  }
}
