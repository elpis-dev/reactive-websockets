package org.elpis.reactive.websockets.impl.util;

import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonMapperTest {
    @Test
    public void applyWithFallback() {
        final String result = JsonMapper.applyWithFallback(Map.of("test", "test"));

        assertThat(result)
                .isEqualTo("{\"test\":\"test\"}");
    }

    @Test
    public void applyWithMono() {
        final Mono<String> result = JsonMapper.applyWithMono(Map.of("test", "test"));

        assertThat(result.block())
                .isEqualTo("{\"test\":\"test\"}");
    }

    @Test
    public void applyWithFlux() {
        final Flux<String> result = JsonMapper.applyWithFlux(Map.of("test", "test"));

        assertThat(result.blockFirst())
                .isEqualTo("{\"test\":\"test\"}");
    }
}
