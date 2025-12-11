package io.github.elpis.reactive.websockets.impl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JsonMapperTest {
  @Test
  public void applyWithFallback() {
    final String result = JsonMapper.applyWithFallback(Map.of("test", "test"));

    assertThat(result).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  public void applyWithMono() {
    final Mono<String> result = JsonMapper.applyWithMono(Map.of("test", "test"));

    assertThat(result.block()).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  public void applyWithFlux() {
    final Flux<String> result = JsonMapper.applyWithFlux(Map.of("test", "test"));

    assertThat(result.blockFirst()).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  public void applyWithFallbackThrowsExceptionWhenCannotProcess() {
    // Create an object that cannot be serialized (e.g., object with circular reference)
    final UnserializableObject unserializable = new UnserializableObject();

    assertThatThrownBy(() -> JsonMapper.applyWithFallback(unserializable))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to translate")
        .hasMessageContaining("UnserializableObject");
  }

  @Test
  public void applyWithDefaultReturnsDefaultValueWhenCannotProcess() {
    final UnserializableObject unserializable = new UnserializableObject();
    final String defaultValue = "DEFAULT_VALUE";

    final String result = JsonMapper.applyWithDefault(unserializable, defaultValue);

    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void applyWithMonoReturnsEmptyWhenCannotProcess() {
    final UnserializableObject unserializable = new UnserializableObject();

    final Mono<String> result = JsonMapper.applyWithMono(unserializable);

    assertThat(result.blockOptional()).isEmpty();
  }

  @Test
  public void applyWithFluxReturnsEmptyWhenCannotProcess() {
    final UnserializableObject unserializable = new UnserializableObject();

    final Flux<String> result = JsonMapper.applyWithFlux(unserializable);

    assertThat(result.collectList().block()).isEmpty();
  }

  /** Test class that cannot be serialized by Jackson ObjectMapper. */
  private static class UnserializableObject {
    // Adding a field with a circular reference or non-serializable type
    private final UnserializableObject self = this;
  }
}
