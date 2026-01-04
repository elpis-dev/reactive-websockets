package io.github.elpis.reactive.websockets.context.controller;

import io.github.elpis.reactive.websockets.template.ReactiveWebSocketTemplate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/broadcast")
public class BroadcastController {
  private final ReactiveWebSocketTemplate template;

  public BroadcastController(ReactiveWebSocketTemplate template) {
    this.template = template;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<String> sendBroadcast(@RequestBody String message) {
    return template.sendBroadcast("/template/broadcast/single", message).then(Mono.empty());
  }

  @PostMapping("/many")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<String> sendBroadcastMany(@RequestBody List<String> messages) {
    return template
        .sendBroadcast("/template/broadcast/many", Flux.fromIterable(messages))
        .then(Mono.empty());
  }

  @PostMapping("/error")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<String> sendBroadcastError(@RequestBody String message) {
    return template.broadcastError("/template/error/broadcast", message).then(Mono.empty());
  }
}
