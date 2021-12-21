package org.elpis.reactive.websockets.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class that combines Jackson mapper features with reactive stack.
 *
 * @author Alex Zharkov
 * @see ObjectMapper
 * @since 0.1.0
 */
@Component
public class JsonMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts object to JSON string. If not possible to convert - throws {@link RuntimeJsonMappingException}.
     * {@link String} type parameters are returned as they are.
     *
     * @since 0.1.0
     */
    public String applyWithFallback(final Object object) {
        try {
            return String.class.isAssignableFrom(object.getClass())
                    ? TypeUtils.cast(object, String.class)
                    : this.objectMapper.writeValueAsString(object);
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
    public String applyWithDefault(final Object object, final String defaultValue) {
        try {
            return String.class.isAssignableFrom(object.getClass())
                    ? TypeUtils.cast(object, String.class)
                    : this.objectMapper.writeValueAsString(object);
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
    public Mono<String> applyWithMono(final Object object) {
        try {
            final String value = String.class.isAssignableFrom(object.getClass())
                    ? TypeUtils.cast(object, String.class)
                    : this.objectMapper.writeValueAsString(object);

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
    public Flux<String> applyWithFlux(final Object object) {
        try {
            final String value = String.class.isAssignableFrom(object.getClass())
                    ? TypeUtils.cast(object, String.class)
                    : this.objectMapper.writeValueAsString(object);

            return Flux.just(value);
        } catch (JsonProcessingException e) {
            return Flux.empty();
        }
    }
}
