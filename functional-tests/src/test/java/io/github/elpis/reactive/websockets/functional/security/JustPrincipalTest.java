package io.github.elpis.reactive.websockets.functional.security;

import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.security.SecurityChainResource;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import io.github.elpis.reactive.websockets.context.security.model.TestPrincipal;
import io.github.elpis.reactive.websockets.functional.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.security.ReactiveWebSocketHandshakeService;
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
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({JustPrincipalTest.PrincipalWebFilterConfiguration.class, SecurityChainResource.class})
class JustPrincipalTest extends BaseWebSocketTest {

  @Test
  void principalAuthenticationTest() throws Exception {
    // given
    final String path = "/auth/security/principal";
    final Sinks.One<String> sink = Sinks.one();

    // expected
    final String expected = TestPrincipal.class.getName();

    // test
    this.withClient(
            path,
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

  @TestConfiguration
  static class PrincipalWebFilterConfiguration {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
      return http.authorizeExchange(exchange -> exchange.anyExchange().permitAll())
          .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
          .build();
    }

    @Bean
    ReactiveWebSocketHandshakeService socketHandshakeService() {
      return ReactiveWebSocketHandshakeService.builder()
          .handshake(
              (exchange, chain) ->
                  chain.filter(exchange.mutate().principal(Mono.just(new TestPrincipal())).build()))
          .build(new ReactorNettyRequestUpgradeStrategy());
    }
  }
}
