package org.elpis.reactive.websockets.impl.security;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.security.SecurityChainResource;
import org.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import org.elpis.reactive.websockets.context.security.model.TestPrincipal;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({JustPrincipalTest.PrincipalWebFilterConfiguration.class, SecurityChainResource.class})
class JustPrincipalTest extends BaseWebSocketTest {

    @Test
    void principalAuthenticationTest() throws Exception {
        //given
        final String path = "/auth/security/principal";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = TestPrincipal.class.getName();

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(sink::tryEmitValue)
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
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
        SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder()
                    .handshake((exchange, chain) -> chain.filter(exchange.mutate().principal(Mono.just(new TestPrincipal())).build()))
                    .build();
        }
    }

}
