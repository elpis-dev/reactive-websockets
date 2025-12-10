package io.github.elpis.reactive.websockets.impl.security;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.security.WebFilterSecurityResource;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import io.github.elpis.reactive.websockets.context.security.model.TestConstants;
import io.github.elpis.reactive.websockets.security.SocketHandshakeService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({
  WebFilterCustomSocketTest.WebFilterAttributeTestSecurityConfiguration.class,
  WebFilterSecurityResource.class
})
class WebFilterCustomSocketTest extends BaseWebSocketTest {

  @Test
  void withWebSocketPrincipalTest() throws Exception {
    // given
    final String path = "/auth/filter/withWebSocketPrincipal";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = TestConstants.TEST_VALUE;

    // test
    this.withClient(
            path,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(sink::tryEmitValue)
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void withExtractedAuthenticationTest() throws Exception {
    // given
    final String path = "/auth/filter/withExtractedAuthentication";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = TestConstants.TEST_VALUE;

    // test
    this.withClient(
            path,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .log()
                    .doOnNext(sink::tryEmitValue)
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .log()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @TestConfiguration
  static class WebFilterAttributeTestSecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
      return http.authorizeExchange(exchange -> exchange.anyExchange().permitAll())
          .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
          .build();
    }

    @Bean
    SocketHandshakeService socketHandshakeService() {
      final WebFilter webFilter =
          (exchange, chain) -> {
            exchange.getAttributes().put(TestConstants.PRINCIPAL, TestConstants.TEST_VALUE);
            return chain.filter(exchange);
          };

      return SocketHandshakeService.builder()
          .handshake(
              webFilter::filter,
              serverWebExchange ->
                  Mono.justOrEmpty(
                      Optional.ofNullable(serverWebExchange.getAttribute(TestConstants.PRINCIPAL))))
          .build(new ReactorNettyRequestUpgradeStrategy());
    }
  }
}
