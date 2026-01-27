package io.github.elpis.reactive.websockets.functional;

import io.github.elpis.reactive.websockets.security.ReactiveWebSocketHandshakeService;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Mono;

public abstract class BaseWebSocketTest {
  public static final String DEFAULT_TEST_PROFILE = "test";

  protected static final Duration DEFAULT_GENERIC_TEST_FALLBACK = Duration.ofSeconds(6L);
  protected static final Duration DEFAULT_FAST_TEST_FALLBACK = Duration.ofSeconds(2L);

  private final Random random = new Random();

  @LocalServerPort private Integer port;

  public WebTestClient getWebClient() {
    return WebTestClient.bindToServer().baseUrl("http://localhost:" + this.port).build();
  }

  public Mono<Void> withClient(
      @NonNull final String path,
      @NonNull final Function<WebSocketSession, Mono<Void>> webSocketHandler)
      throws URISyntaxException {

    return new ReactorNettyWebSocketClient().execute(this.getUrl(path), webSocketHandler::apply);
  }

  public Mono<Void> withClient(
      @NonNull final String path,
      @NonNull final HttpHeaders headers,
      @NonNull final Function<WebSocketSession, Mono<Void>> webSocketHandler)
      throws URISyntaxException {

    return new ReactorNettyWebSocketClient()
        .execute(this.getUrl(path), headers, webSocketHandler::apply);
  }

  public Mono<Void> withClient(
      @NonNull final String path,
      @NonNull final MultiValueMap<String, HttpCookie> cookies,
      @NonNull final Function<WebSocketSession, Mono<Void>> webSocketHandler)
      throws URISyntaxException {

    HttpHeaders headers = new HttpHeaders();
    headers.add(
        HttpHeaders.COOKIE,
        cookies.values().stream()
            .flatMap(Collection::stream)
            .map(cookie -> cookie.getName() + "=" + cookie.getValue())
            .collect(Collectors.joining("; ")));

    return this.withClient(path, headers, webSocketHandler);
  }

  // TODO: Extract to utility class
  public String randomTextString(final int length) {
    int leftLimit = 97;
    int rightLimit = 122;

    return random
        .ints(leftLimit, rightLimit + 1)
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
  public static class PermitAllSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
      return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
          .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
          .build();
    }

    @Bean
    public ReactiveWebSocketHandshakeService socketHandshakeService() {
      return ReactiveWebSocketHandshakeService.builder()
          .build(new ReactorNettyRequestUpgradeStrategy());
    }
  }
}
