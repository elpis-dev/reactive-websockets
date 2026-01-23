package io.github.elpis.reactive.websockets.impl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import io.github.elpis.reactive.websockets.config.mapper.ReactiveWebSocketMappingConfiguration;
import io.github.elpis.reactive.websockets.mapper.JsonMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ReactiveWebSocketMappingConfiguration.class)
class JsonMapperTest {

  @Autowired private JsonMapper jsonMapper;

  @Test
  void testApplyWithFallbackSerializesMap() {
    final String result = jsonMapper.applyWithFallback(Map.of("test", "test"));

    assertThat(result).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  void testApplyWithFallbackSerializesComplexObject() {
    final TestPojo pojo = new TestPojo("John", 30, List.of("reading", "coding"));

    final String result = jsonMapper.applyWithFallback(pojo);

    assertThat(result)
        .contains("\"name\":\"John\"")
        .contains("\"age\":30")
        .contains("\"hobbies\":[\"reading\",\"coding\"]");
  }

  @Test
  void testApplyWithFallbackReturnsStringAsIs() {
    final String input = "already a string";

    final String result = jsonMapper.applyWithFallback(input);

    assertThat(result).isEqualTo(input);
  }

  @Test
  void testApplyWithFallbackThrowsExceptionWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();

    assertThatThrownBy(() -> jsonMapper.applyWithFallback(unserializable))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to translate")
        .hasMessageContaining("UnserializableObject");
  }

  @Test
  void testApplyWithDefaultSerializesObject() {
    final String result = jsonMapper.applyWithDefault(Map.of("key", "value"), "default");

    assertThat(result).isEqualTo("{\"key\":\"value\"}");
  }

  @Test
  void testApplyWithDefaultReturnsDefaultValueWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();
    final String defaultValue = "DEFAULT_VALUE";

    final String result = jsonMapper.applyWithDefault(unserializable, defaultValue);

    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  void testApplyWithDefaultReturnsStringAsIs() {
    final String input = "test string";

    final String result = jsonMapper.applyWithDefault(input, "default");

    assertThat(result).isEqualTo(input);
  }

  @Test
  void testApplyWithMonoSerializesObject() {
    final Mono<String> result = jsonMapper.applyWithMono(Map.of("test", "test"));

    assertThat(result.block()).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  void testApplyWithMonoReturnsEmptyWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();

    final Mono<String> result = jsonMapper.applyWithMono(unserializable);

    assertThat(result.blockOptional()).isEmpty();
  }

  @Test
  void testApplyWithMonoReturnsStringAsIs() {
    final String input = "test string";

    final Mono<String> result = jsonMapper.applyWithMono(input);

    assertThat(result.block()).isEqualTo(input);
  }

  @Test
  void testApplyWithFluxSerializesObject() {
    final Flux<String> result = jsonMapper.applyWithFlux(Map.of("test", "test"));

    assertThat(result.blockFirst()).isEqualTo("{\"test\":\"test\"}");
  }

  @Test
  void testApplyWithFluxReturnsEmptyWhenCannotSerialize() {
    final UnserializableObject unserializable = new UnserializableObject();

    final Flux<String> result = jsonMapper.applyWithFlux(unserializable);

    assertThat(result.collectList().block()).isEmpty();
  }

  @Test
  void testApplyWithFluxReturnsStringAsIs() {
    final String input = "test string";

    final Flux<String> result = jsonMapper.applyWithFlux(input);

    assertThat(result.blockFirst()).isEqualTo(input);
  }

  @Test
  void testDeserializeDeserializesSimpleObject() {
    final String json = "{\"name\":\"Alice\",\"age\":25,\"hobbies\":[\"music\",\"sports\"]}";

    final TestPojo result = jsonMapper.deserialize(json, TestPojo.class);

    assertThat(result.getName()).isEqualTo("Alice");
    assertThat(result.getAge()).isEqualTo(25);
    assertThat(result.getHobbies()).containsExactly("music", "sports");
  }

  @Test
  void testDeserializeDeserializesMap() {
    final String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

    @SuppressWarnings("unchecked")
    final Map<String, String> result = jsonMapper.deserialize(json, Map.class);

    assertThat(result).containsEntry("key1", "value1").containsEntry("key2", "value2");
  }

  @Test
  void testDeserializeReturnsStringAsIsForStringClass() {
    final String json = "just a plain string";

    final String result = jsonMapper.deserialize(json, String.class);

    assertThat(result).isEqualTo(json);
  }

  @Test
  void testDeserializeDeserializesNestedObject() {
    final String json =
        "{\"person\":{\"name\":\"Bob\",\"age\":40,\"hobbies\":[]},\"location\":\"NYC\"}";

    final NestedPojo result = jsonMapper.deserialize(json, NestedPojo.class);

    assertThat(result.getPerson().getName()).isEqualTo("Bob");
    assertThat(result.getPerson().getAge()).isEqualTo(40);
    assertThat(result.getLocation()).isEqualTo("NYC");
  }

  @Test
  void testDeserializeThrowsExceptionForInvalidJson() {
    final String invalidJson = "{invalid json}";

    assertThatThrownBy(() -> jsonMapper.deserialize(invalidJson, TestPojo.class))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to deserialize JSON to TestPojo");
  }

  @Test
  void testDeserializeThrowsExceptionForMismatchedType() {
    final String json = "{\"name\":\"Test\",\"age\":\"not a number\",\"getHobbies\":[]}";

    assertThatThrownBy(() -> jsonMapper.deserialize(json, TestPojo.class))
        .isInstanceOf(RuntimeJsonMappingException.class)
        .hasMessageContaining("Unable to deserialize JSON to TestPojo");
  }

  @Test
  void testDeserializeHandlesEmptyObject() {
    final String json = "{}";

    final TestPojo result = jsonMapper.deserialize(json, TestPojo.class);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isNull();
  }

  @Test
  void testDeserializeWithMonoDeserializesObject() {
    final String json = "{\"name\":\"Charlie\",\"age\":35,\"hobbies\":[\"gaming\"]}";

    final Mono<TestPojo> result = jsonMapper.deserializeWithMono(json, TestPojo.class);

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
  void testDeserializeWithMonoReturnsStringAsIs() {
    final String json = "plain text";

    final Mono<String> result = jsonMapper.deserializeWithMono(json, String.class);

    assertThat(result.block()).isEqualTo(json);
  }

  @Test
  void testDeserializeWithMonoReturnsErrorForInvalidJson() {
    final String invalidJson = "not valid json";

    final Mono<TestPojo> result = jsonMapper.deserializeWithMono(invalidJson, TestPojo.class);

    StepVerifier.create(result)
        .expectErrorMatches(
            error ->
                error instanceof RuntimeJsonMappingException
                    && error.getMessage().contains("Unable to deserialize JSON to TestPojo"))
        .verify();
  }

  @Test
  void testDeserializeWithFluxDeserializesObject() {
    final String json = "{\"name\":\"Diana\",\"age\":28,\"hobbies\":[\"yoga\",\"cooking\"]}";

    final Flux<TestPojo> result = jsonMapper.deserializeWithFlux(json, TestPojo.class);

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
  void testDeserializeWithFluxReturnsStringAsIs() {
    final String json = "plain text";

    final Flux<String> result = jsonMapper.deserializeWithFlux(json, String.class);

    assertThat(result.blockFirst()).isEqualTo(json);
  }

  @Test
  void testDeserializeWithFluxReturnsErrorForInvalidJson() {
    final String invalidJson = "{broken json";

    final Flux<TestPojo> result = jsonMapper.deserializeWithFlux(invalidJson, TestPojo.class);

    StepVerifier.create(result)
        .expectErrorMatches(
            error ->
                error instanceof RuntimeJsonMappingException
                    && error.getMessage().contains("Unable to deserialize JSON to TestPojo"))
        .verify();
  }

  @Test
  void testRoundTripSerializeAndDeserialize() {
    final TestPojo original = new TestPojo("Eve", 45, List.of("travel", "photography"));

    final String json = jsonMapper.applyWithFallback(original);
    final TestPojo deserialized = jsonMapper.deserialize(json, TestPojo.class);

    assertThat(deserialized.getName()).isEqualTo(original.getName());
    assertThat(deserialized.getAge()).isEqualTo(original.getAge());
    assertThat(deserialized.getHobbies()).isEqualTo(original.getHobbies());
  }

  @Test
  void testRoundTripWithReactiveMono() {
    final TestPojo original = new TestPojo("Frank", 50, List.of("golf"));

    final Mono<String> serialized = jsonMapper.applyWithMono(original);
    final Mono<TestPojo> deserialized =
        serialized.flatMap(json -> jsonMapper.deserializeWithMono(json, TestPojo.class));

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
  void testRoundTripWithReactiveFlux() {
    final TestPojo original = new TestPojo("Grace", 32, List.of("painting", "dancing"));

    final Flux<String> serialized = jsonMapper.applyWithFlux(original);
    final Flux<TestPojo> deserialized =
        serialized.flatMap(json -> jsonMapper.deserializeWithFlux(json, TestPojo.class));

    StepVerifier.create(deserialized)
        .assertNext(
            pojo -> {
              assertThat(pojo.getName()).isEqualTo(original.getName());
              assertThat(pojo.getAge()).isEqualTo(original.getAge());
              assertThat(pojo.getHobbies()).isEqualTo(original.getHobbies());
            })
        .verifyComplete();
  }

  /** Test class that cannot be serialized by Jackson ObjectMapper. */
  private static class UnserializableObject {
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
