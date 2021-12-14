package org.elpis.socket.web.impl.security;

import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.resource.security.SecurityChainResource;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.elpis.socket.web.context.security.model.TestPrincipal;
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
public class JustPrincipalTest extends BaseWebSocketTest {

    @Test
    public void principalAuthenticationTest() throws Exception {
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
    public static class PrincipalWebFilterConfiguration {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
            return http.authorizeExchange()
                    .anyExchange().permitAll()
                    .and()
                    .formLogin()
                    .disable()
                    .build();
        }

        @Bean
        public SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder()
                    .handshake((exchange, chain) -> chain.filter(exchange.mutate().principal(Mono.just(new TestPrincipal())).build()))
                    .build();
        }
    }

}
