package org.elpis.reactive.websockets.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class JsonMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String applyWithFallback(final Object object) {
        try {
            return String.class.isAssignableFrom(object.getClass())
                    ? TypeUtils.cast(object, String.class)
                    : this.objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonMappingException("Unable to translate " + object.getClass() + " instance to String.class");
        }
    }

    public String applyWithDefault(final Object object, final String defaultValue) {
        try {
            return this.objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return defaultValue;
        }
    }

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
