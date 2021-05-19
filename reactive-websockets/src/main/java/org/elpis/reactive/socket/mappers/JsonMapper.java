package org.elpis.reactive.socket.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import reactor.core.publisher.Flux;

public class JsonMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String applyWithFallback(final Object object) {
        try {
            return this.objectMapper.writeValueAsString(object);
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

    public Flux<String> applyWithFlux(final Object object) {
        try {
            return Flux.just(this.objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return Flux.empty();
        }
    }
}
