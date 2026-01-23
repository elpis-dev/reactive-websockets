package io.github.elpis.reactive.websockets.context.resource.data;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;

@MessageEndpoint("/path")
public class PathVariableSocketResource {

  @OnMessage(value = "/single/get/string/{id}")
  public Publisher<?> getWithStringPath(@PathVariable("id") final String id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/no/string/{id}")
  public Publisher<?> getWithNoStringPath(@PathVariable("ids") final String id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/byte/{id}")
  public Publisher<?> getWithNumericBytePath(@PathVariable("id") final Byte id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/byte/{id}")
  public Publisher<?> getWithNumericPrimitiveBytePath(@PathVariable("id") final byte id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/short/{id}")
  public Publisher<?> getWithNumericShortPath(@PathVariable("id") final Short id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/short/{id}")
  public Publisher<?> getWithNumericShortPrimitivePath(@PathVariable("id") final short id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/int/{id}")
  public Publisher<?> getWithNumericIntPath(@PathVariable("id") final Integer id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/int/{id}")
  public Publisher<?> getWithNumericIntPrimitivePath(@PathVariable("id") final int id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/long/{id}")
  public Publisher<?> getWithNumericLongPath(@PathVariable("id") final Long id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/numeric/primitive/long/{id}")
  public Publisher<?> getWithNumericLongPrimitivePath(@PathVariable("id") final long id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/boolean/{id}")
  public Publisher<?> getWithBooleanPath(@PathVariable("id") final Boolean id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/boolean/primitive/{id}")
  public Publisher<?> getWithBooleanPrimitivePath(@PathVariable("id") final boolean id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/enum/{id}")
  public Publisher<?> getEnumPath(@PathVariable("id") final BootStarter.Example id) {
    return Flux.just(Map.of("path", id));
  }

  @OnMessage(value = "/single/get/no/required/{id}")
  public Publisher<?> getWithNoRequiredPath(
      @PathVariable(value = "no", required = false) final String id) {
    return Flux.just(Map.of("path", String.valueOf(id)));
  }
}
