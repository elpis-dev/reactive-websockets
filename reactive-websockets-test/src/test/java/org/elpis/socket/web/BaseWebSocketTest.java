
package org.elpis.socket.web;

import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

public abstract class BaseWebSocketTest {
    public static final String DEFAULT_TEST_PROFILE = "test";

    protected static final Duration DEFAULT_LONG_TEST_FALLBACK = Duration.ofSeconds(30L);
    protected static final Duration DEFAULT_GENERIC_TEST_FALLBACK = Duration.ofSeconds(10L);
    protected static final Duration DEFAULT_FAST_TEST_FALLBACK = Duration.ofSeconds(5L);

    private final Random random = new Random();

    @LocalServerPort
    private Integer port;

    public Mono<Void> withClient(@NonNull final String path, @NonNull final Function<WebSocketSession, Mono<Void>> webSocketHandler)
            throws URISyntaxException {

        return new ReactorNettyWebSocketClient().execute(this.getUrl(path), webSocketHandler::apply);
    }

    public Mono<Void> withClient(@NonNull final String path, @NonNull final HttpHeaders headers,
                                 @NonNull final Function<WebSocketSession, Mono<Void>> webSocketHandler) throws URISyntaxException {

        return new ReactorNettyWebSocketClient().execute(this.getUrl(path), headers, webSocketHandler::apply);
    }

    public String randomTextString(final int length) {
        int leftLimit = 97;
        int rightLimit = 122;

        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public byte getRandomByte() {
        return (byte) random.nextInt(Byte.MAX_VALUE - 1);
    }

    public short getRandomShort() {
        return (short) random.nextInt(Short.MAX_VALUE - 1);
    }

    public int getRandomInteger() {
        return random.nextInt();
    }

    public long getRandomLong() {
        return random.nextLong();
    }

    public double getRandomDouble() {
        return random.nextDouble();
    }

    public float getRandomFloat() {
        return random.nextFloat();
    }

    protected URI getUrl(@NonNull final String path) throws URISyntaxException {
        return new URI("ws://localhost:" + this.port + path);
    }

    @TestConfiguration
    @EnableReactiveSockets
    public static class PermitAllSecurityConfiguration {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
            return http.authorizeExchange().anyExchange().permitAll().and().build();
        }

        @Bean
        public SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder().build();
        }

    }

}
