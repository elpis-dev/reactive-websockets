package io.github.elpis.reactive.websockets.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class that combines Jackson mapper features with reactive stack.
 *
 * @author Phillip J. Fry
 * @see ObjectMapper
 * @since 1.0.0
 */
public class JsonMapper {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private JsonMapper() {}

  /**
   * Converts object to JSON string. If not possible to convert - throws {@link
   * RuntimeJsonMappingException}. {@link String} type parameters are returned as they are.
   *
   * @since 1.0.0
   */
  public static String applyWithFallback(final Object object) {
    try {
      return String.class.isAssignableFrom(object.getClass())
          ? (String) object
          : objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeJsonMappingException(
          "Unable to translate " + object.getClass() + " instance to String.class");
    }
  }

  /**
   * Converts object to JSON string. If not possible to convert - returns default value. {@link
   * String} type parameters are returned as they are.
   *
   * @since 1.0.0
   */
  public static String applyWithDefault(final Object object, final String defaultValue) {
    try {
      return String.class.isAssignableFrom(object.getClass())
          ? (String) object
          : objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      return defaultValue;
    }
  }

  /**
   * Converts object to JSON string and returns a {@link Mono} instance with converted value. If not
   * possible to convert - returns {@link Mono#empty()}. {@link String} type parameters are returned
   * as they are, wrapped into {@link Mono} instance.
   *
   * @since 1.0.0
   */
  public static Mono<String> applyWithMono(final Object object) {
    try {
      final String value =
          String.class.isAssignableFrom(object.getClass())
              ? (String) object
              : objectMapper.writeValueAsString(object);

      return Mono.just(value);
    } catch (JsonProcessingException e) {
      return Mono.empty();
    }
  }

  /**
   * Converts object to JSON string and returns a {@link Flux} instance with converted value. If not
   * possible to convert - returns {@link Flux#empty()}. {@link String} type parameters are returned
   * as they are, wrapped into {@link Flux} instance.
   *
   * @since 1.0.0
   */
  public static Flux<String> applyWithFlux(final Object object) {
    try {
      final String value =
          String.class.isAssignableFrom(object.getClass())
              ? (String) object
              : objectMapper.writeValueAsString(object);

      return Flux.just(value);
    } catch (JsonProcessingException e) {
      return Flux.empty();
    }
  }

  /**
   * Deserializes JSON string to the specified type. If not possible to deserialize - throws {@link
   * RuntimeJsonMappingException}. {@link String} type parameters are returned as they are.
   *
   * @param json the JSON string to deserialize
   * @param clazz the target class type
   * @param <T> the type of the deserialized object
   * @return the deserialized object
   * @throws RuntimeJsonMappingException if deserialization fails
   * @since 1.0.0
   */
  public static <T> T deserialize(final String json, final Class<T> clazz) {
    try {
      return String.class.equals(clazz) ? clazz.cast(json) : objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeJsonMappingException(
          "Unable to deserialize JSON to " + clazz.getSimpleName() + ": " + e.getMessage());
    }
  }

  /**
   * Deserializes JSON string to the specified type and returns a {@link Mono}. If not possible to
   * deserialize - returns {@link Mono#error(Throwable)}. {@link String} type parameters are
   * returned as they are, wrapped into {@link Mono}.
   *
   * @param json the JSON string to deserialize
   * @param clazz the target class type
   * @param <T> the type of the deserialized object
   * @return a Mono containing the deserialized object or an error
   * @since 1.0.0
   */
  public static <T> Mono<T> deserializeWithMono(final String json, final Class<T> clazz) {
    try {
      final T value =
          String.class.equals(clazz) ? clazz.cast(json) : objectMapper.readValue(json, clazz);
      return Mono.just(value);
    } catch (JsonProcessingException e) {
      return Mono.error(
          new RuntimeJsonMappingException(
              "Unable to deserialize JSON to " + clazz.getSimpleName() + ": " + e.getMessage()));
    }
  }

  /**
   * Deserializes JSON string to the specified type and returns a {@link Flux}. If not possible to
   * deserialize - returns {@link Flux#error(Throwable)}. {@link String} type parameters are
   * returned as they are, wrapped into {@link Flux}.
   *
   * @param json the JSON string to deserialize
   * @param clazz the target class type
   * @param <T> the type of the deserialized object
   * @return a Flux containing the deserialized object or an error
   * @since 1.0.0
   */
  public static <T> Flux<T> deserializeWithFlux(final String json, final Class<T> clazz) {
    try {
      final T value =
          String.class.equals(clazz) ? clazz.cast(json) : objectMapper.readValue(json, clazz);
      return Flux.just(value);
    } catch (JsonProcessingException e) {
      return Flux.error(
          new RuntimeJsonMappingException(
              "Unable to deserialize JSON to " + clazz.getSimpleName() + ": " + e.getMessage()));
    }
  }
}
