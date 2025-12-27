package io.github.elpis.reactive.websockets.impl.security;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.security.SecurityChainResource;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import io.github.elpis.reactive.websockets.context.security.model.TestConstants;
import io.github.elpis.reactive.websockets.security.SocketHandshakeService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({
  SecurityChainTest.SecurityChainTestSecurityConfiguration.class,
  SecurityChainResource.class
})
class SecurityChainTest extends BaseWebSocketTest {

  @Test
  void withExtractedAuthenticationValidChainTest() throws Exception {
    // given
    final HttpHeaders headers = new HttpHeaders();
    headers.add(TestConstants.PRINCIPAL, TestConstants.TEST_VALUE);

    final String path = "/auth/security/withExtractedAuthentication";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = TestConstants.TEST_VALUE;

    // test
    this.withClient(
            path,
            headers,
            (session) ->
                session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(sink::tryEmitValue)
                    .then())
        .subscribe();

    // verify
    StepVerifier.create(sink.asMono())
        .expectNext(expected)
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @Test
  void withExtractedAuthenticationChainValidationUnauthorizedTest() throws Exception {
    // given
    final HttpHeaders headers = new HttpHeaders();
    headers.add(TestConstants.PRINCIPAL, UUID.randomUUID().toString());

    final String path = "/auth/security/withExtractedAuthentication";
    final Sinks.One<Throwable> errorSink = Sinks.one();

    // test - authentication should fail due to wrong principal value
    this.withClient(path, headers, (session) -> session.receive().then())
        .doOnError(errorSink::tryEmitValue)
        .subscribe();

    // verify - expect WebSocketProcessingException wrapped in handshake error
    StepVerifier.create(errorSink.asMono())
        .expectNextMatches(
            throwable ->
                throwable
                    .getMessage()
                    .contains("Invalid handshake response getStatus: 401 Unauthorized"))
        .expectComplete()
        .verify(DEFAULT_GENERIC_TEST_FALLBACK);
  }

  @TestConfiguration
  static class SecurityChainTestSecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
      return http.authorizeExchange(
              authorizeExchangeSpec ->
                  authorizeExchangeSpec
                      .anyExchange()
                      .access(
                          (authentication, context) -> {
                            final boolean hasValidHeader =
                                Optional.ofNullable(
                                        context
                                            .getExchange()
                                            .getRequest()
                                            .getHeaders()
                                            .get(TestConstants.PRINCIPAL))
                                    .flatMap(headers -> headers.stream().findFirst())
                                    .map(TestConstants.TEST_VALUE::equals)
                                    .orElse(false);

                            return Mono.just(new AuthorizationDecision(hasValidHeader));
                          }))
          .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
          .build();
    }

    @Bean
    SocketHandshakeService socketHandshakeService() {
      return SocketHandshakeService.builder()
          .handshake(
              serverWebExchange ->
                  Mono.justOrEmpty(
                      Optional.ofNullable(
                              serverWebExchange
                                  .getRequest()
                                  .getHeaders()
                                  .get(TestConstants.PRINCIPAL))
                          .flatMap(headers -> headers.stream().findFirst())))
          .build(new ReactorNettyRequestUpgradeStrategy());
    }
  }
}
