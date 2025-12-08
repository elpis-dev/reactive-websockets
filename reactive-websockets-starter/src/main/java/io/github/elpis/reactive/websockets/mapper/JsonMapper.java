package io.github.elpis.reactive.websockets.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class that combines Jackson mapper features with reactive stack.
 *
 * @author Alex Zharkov
 * @see ObjectMapper
 * @since 0.1.0
 */
public class JsonMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonMapper() {
    }

    /**
     * Converts object to JSON string. If not possible to convert - throws {@link RuntimeJsonMappingException}.
     * {@link String} type parameters are returned as they are.
     *
     * @since 0.1.0
     */
    public static String applyWithFallback(final Object object) {
        try {
            return String.class.isAssignableFrom(object.getClass())
                    ? (String) object
                    : objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonMappingException("Unable to translate " + object.getClass() + " instance to String.class");
        }
    }

    /**
     * Converts object to JSON string. If not possible to convert - returns default value.
     * {@link String} type parameters are returned as they are.
     *
     * @since 0.1.0
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
     * Converts object to JSON string and returns a {@link Mono} instance with converted value. If not possible to convert - returns {@link Mono#empty()}.
     * {@link String} type parameters are returned as they are, wrapped into {@link Mono} instance.
     *
     * @since 0.1.0
     */
    public static Mono<String> applyWithMono(final Object object) {
        try {
            final String value = String.class.isAssignableFrom(object.getClass())
                    ? (String) object
                    : objectMapper.writeValueAsString(object);

            return Mono.just(value);
        } catch (JsonProcessingException e) {
            return Mono.empty();
        }
    }


    /**
     * Converts object to JSON string and returns a {@link Flux} instance with converted value. If not possible to convert - returns {@link Flux#empty()}.
     * {@link String} type parameters are returned as they are, wrapped into {@link Flux} instance.
     *
     * @since 0.1.0
     */
    public static Flux<String> applyWithFlux(final Object object) {
        try {
            final String value = String.class.isAssignableFrom(object.getClass())
                    ? (String) object
                    : objectMapper.writeValueAsString(object);

            return Flux.just(value);
        } catch (JsonProcessingException e) {
            return Flux.empty();
        }
    }
}
