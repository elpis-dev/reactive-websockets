package io.github.elpis.reactive.websockets.impl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class JsonMapperTest {

  // ========== Serialization Tests (applyWith*) ==========

  @Test
  public void applyWithFallback_shouldSerializeMap() {
    final String result = JsonMapper.applyWithFallback(Map.of("test", "test"));

    assertThat(result).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  public void applyWithFallback_shouldSerializeComplexObject() {
    final TestPojo pojo = new TestPojo("John", 30, List.of("reading", "coding"));

    final String result = JsonMapper.applyWithFallback(pojo);

    assertThat(result)
        .contains("\"name\":\"John\"")
        .contains("\"age\":30")
        .contains("\"hobbies\":[\"reading\",\"coding\"]");
  }

  @Test
  public void applyWithFallback_shouldReturnStringAsIs() {
    final String input = "already a string";

    final String result = JsonMapper.applyWithFallback(input);

    assertThat(result).isEqualTo(input);
  }

  @Test
  public void applyWithFallback_shouldThrowExceptionWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();

    assertThatThrownBy(() -> JsonMapper.applyWithFallback(unserializable))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to translate")
        .hasMessageContaining("UnserializableObject");
  }

  @Test
  public void applyWithDefault_shouldSerializeObject() {
    final String result = JsonMapper.applyWithDefault(Map.of("key", "value"), "default");

    assertThat(result).isEqualTo("{\"key\":\"value\"}");
  }

  @Test
  public void applyWithDefault_shouldReturnDefaultValueWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();
    final String defaultValue = "DEFAULT_VALUE";

    final String result = JsonMapper.applyWithDefault(unserializable, defaultValue);

    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void applyWithDefault_shouldReturnStringAsIs() {
    final String input = "test string";

    final String result = JsonMapper.applyWithDefault(input, "default");

    assertThat(result).isEqualTo(input);
  }

  @Test
  public void applyWithMono_shouldSerializeObject() {
    final Mono<String> result = JsonMapper.applyWithMono(Map.of("test", "test"));

    assertThat(result.block()).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  public void applyWithMono_shouldReturnEmptyWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();

    final Mono<String> result = JsonMapper.applyWithMono(unserializable);

    assertThat(result.blockOptional()).isEmpty();
  }

  @Test
  public void applyWithMono_shouldReturnStringAsIs() {
    final String input = "test string";

    final Mono<String> result = JsonMapper.applyWithMono(input);

    assertThat(result.block()).isEqualTo(input);
  }

  @Test
  public void applyWithFlux_shouldSerializeObject() {
    final Flux<String> result = JsonMapper.applyWithFlux(Map.of("test", "test"));

    assertThat(result.blockFirst()).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  public void applyWithFlux_shouldReturnEmptyWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();

    final Flux<String> result = JsonMapper.applyWithFlux(unserializable);

    assertThat(result.collectList().block()).isEmpty();
  }

  @Test
  public void applyWithFlux_shouldReturnStringAsIs() {
    final String input = "test string";

    final Flux<String> result = JsonMapper.applyWithFlux(input);

    assertThat(result.blockFirst()).isEqualTo(input);
  }

  // ========== Deserialization Tests (deserialize*) ==========

  @Test
  public void deserialize_shouldDeserializeSimpleObject() {
    final String json = "{\"name\":\"Alice\",\"age\":25,\"hobbies\":[\"music\",\"sports\"]}";

    final TestPojo result = JsonMapper.deserialize(json, TestPojo.class);

    assertThat(result.getName()).isEqualTo("Alice");
    assertThat(result.getAge()).isEqualTo(25);
    assertThat(result.getHobbies()).containsExactly("music", "sports");
  }

  @Test
  public void deserialize_shouldDeserializeMap() {
    final String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

    @SuppressWarnings("unchecked")
    final Map<String, String> result = JsonMapper.deserialize(json, Map.class);

    assertThat(result).containsEntry("key1", "value1").containsEntry("key2", "value2");
  }

  @Test
  public void deserialize_shouldReturnStringAsIsForStringClass() {
    final String json = "just a plain string";

    final String result = JsonMapper.deserialize(json, String.class);

    assertThat(result).isEqualTo(json);
  }

  @Test
  public void deserialize_shouldDeserializeNestedObject() {
    final String json =
        "{\"person\":{\"name\":\"Bob\",\"age\":40,\"hobbies\":[]},\"location\":\"NYC\"}";

    final NestedPojo result = JsonMapper.deserialize(json, NestedPojo.class);

    assertThat(result.getPerson().getName()).isEqualTo("Bob");
    assertThat(result.getPerson().getAge()).isEqualTo(40);
    assertThat(result.getLocation()).isEqualTo("NYC");
  }

  @Test
  public void deserialize_shouldThrowExceptionForInvalidJson() {
    final String invalidJson = "{invalid json}";

    assertThatThrownBy(() -> JsonMapper.deserialize(invalidJson, TestPojo.class))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to deserialize JSON to TestPojo");
  }

  @Test
  public void deserialize_shouldThrowExceptionForMismatchedType() {
    final String json = "{\"name\":\"Test\",\"age\":\"not a number\",\"hobbies\":[]}";

    assertThatThrownBy(() -> JsonMapper.deserialize(json, TestPojo.class))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to deserialize JSON to TestPojo");
  }

  @Test
  public void deserialize_shouldHandleEmptyObject() {
    final String json = "{}";

    final TestPojo result = JsonMapper.deserialize(json, TestPojo.class);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNull();
  }

  @Test
  public void deserializeWithMono_shouldDeserializeObject() {
    final String json = "{\"name\":\"Charlie\",\"age\":35,\"hobbies\":[\"gaming\"]}";

    final Mono<TestPojo> result = JsonMapper.deserializeWithMono(json, TestPojo.class);

    StepVerifier.create(result)
        .assertNext(
            pojo -> {
              assertThat(pojo.getName()).isEqualTo("Charlie");
              assertThat(pojo.getAge()).isEqualTo(35);
              assertThat(pojo.getHobbies()).containsExactly("gaming");
            })
        .verifyComplete();
  }

  @Test
  public void deserializeWithMono_shouldReturnStringAsIs() {
    final String json = "plain text";

    final Mono<String> result = JsonMapper.deserializeWithMono(json, String.class);

    assertThat(result.block()).isEqualTo(json);
  }

  @Test
  public void deserializeWithMono_shouldReturnErrorForInvalidJson() {
    final String invalidJson = "not valid json";

    final Mono<TestPojo> result = JsonMapper.deserializeWithMono(invalidJson, TestPojo.class);

    StepVerifier.create(result)
        .expectErrorMatches(
            error ->
                error instanceof RuntimeJsonMappingException
                    && error.getMessage().contains("Unable to deserialize JSON to TestPojo"))
        .verify();
  }

  @Test
  public void deserializeWithFlux_shouldDeserializeObject() {
    final String json = "{\"name\":\"Diana\",\"age\":28,\"hobbies\":[\"yoga\",\"cooking\"]}";

    final Flux<TestPojo> result = JsonMapper.deserializeWithFlux(json, TestPojo.class);

    StepVerifier.create(result)
        .assertNext(
            pojo -> {
              assertThat(pojo.getName()).isEqualTo("Diana");
              assertThat(pojo.getAge()).isEqualTo(28);
              assertThat(pojo.getHobbies()).containsExactly("yoga", "cooking");
            })
        .verifyComplete();
  }

  @Test
  public void deserializeWithFlux_shouldReturnStringAsIs() {
    final String json = "plain text";

    final Flux<String> result = JsonMapper.deserializeWithFlux(json, String.class);

    assertThat(result.blockFirst()).isEqualTo(json);
  }

  @Test
  public void deserializeWithFlux_shouldReturnErrorForInvalidJson() {
    final String invalidJson = "{broken json";

    final Flux<TestPojo> result = JsonMapper.deserializeWithFlux(invalidJson, TestPojo.class);

    StepVerifier.create(result)
        .expectErrorMatches(
            error ->
                error instanceof RuntimeJsonMappingException
                    && error.getMessage().contains("Unable to deserialize JSON to TestPojo"))
        .verify();
  }

  // ========== Round-trip Tests (Serialize + Deserialize) ==========

  @Test
  public void shouldRoundTripSerializeAndDeserialize() {
    final TestPojo original = new TestPojo("Eve", 45, List.of("travel", "photography"));

    final String json = JsonMapper.applyWithFallback(original);
    final TestPojo deserialized = JsonMapper.deserialize(json, TestPojo.class);

    assertThat(deserialized.getName()).isEqualTo(original.getName());
    assertThat(deserialized.getAge()).isEqualTo(original.getAge());
    assertThat(deserialized.getHobbies()).isEqualTo(original.getHobbies());
  }

  @Test
  public void shouldRoundTripWithReactiveMono() {
    final TestPojo original = new TestPojo("Frank", 50, List.of("golf"));

    final Mono<String> serialized = JsonMapper.applyWithMono(original);
    final Mono<TestPojo> deserialized =
        serialized.flatMap(json -> JsonMapper.deserializeWithMono(json, TestPojo.class));

    StepVerifier.create(deserialized)
        .assertNext(
            pojo -> {
              assertThat(pojo.getName()).isEqualTo(original.getName());
              assertThat(pojo.getAge()).isEqualTo(original.getAge());
              assertThat(pojo.getHobbies()).isEqualTo(original.getHobbies());
            })
        .verifyComplete();
  }

  @Test
  public void shouldRoundTripWithReactiveFlux() {
    final TestPojo original = new TestPojo("Grace", 32, List.of("painting", "dancing"));

    final Flux<String> serialized = JsonMapper.applyWithFlux(original);
    final Flux<TestPojo> deserialized =
        serialized.flatMap(json -> JsonMapper.deserializeWithFlux(json, TestPojo.class));

    StepVerifier.create(deserialized)
        .assertNext(
            pojo -> {
              assertThat(pojo.getName()).isEqualTo(original.getName());
              assertThat(pojo.getAge()).isEqualTo(original.getAge());
              assertThat(pojo.getHobbies()).isEqualTo(original.getHobbies());
            })
        .verifyComplete();
  }

  // ========== Test POJOs ==========

  /** Test class that cannot be serialized by Jackson ObjectMapper. */
  private static class UnserializableObject {
    // Adding a field with a circular reference or non-serializable type
    private final UnserializableObject self = this;
  }

  /** Simple POJO for testing serialization and deserialization. */
  public static class TestPojo {
    private String name;
    private Integer age;
    private List<String> hobbies;

    public TestPojo() {}

    public TestPojo(String name, Integer age, List<String> hobbies) {
      this.name = name;
      this.age = age;
      this.hobbies = hobbies;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getAge() {
      return age;
    }

    public void setAge(Integer age) {
      this.age = age;
    }

    public List<String> getHobbies() {
      return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
      this.hobbies = hobbies;
    }
  }

  /** Nested POJO for testing complex object serialization/deserialization. */
  public static class NestedPojo {
    private TestPojo person;
    private String location;

    public NestedPojo() {}

    public NestedPojo(TestPojo person, String location) {
      this.person = person;
      this.location = location;
    }

    public TestPojo getPerson() {
      return person;
    }

    public void setPerson(TestPojo person) {
      this.person = person;
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
      this.location = location;
    }
  }
}
