package org.elpis.reactive.websockets.impl.security;

import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.security.SecurityChainResource;
import org.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({AnonymousFallbackAuthenticationTest.AnonymousFallbackTestSecurityConfiguration.class, SecurityChainResource.class})
@TestPropertySource("classpath:application-test-disabled-default-security.properties")
class AnonymousFallbackAuthenticationTest extends BaseWebSocketTest {

    @Test
    void anonymousFallbackAuthenticationTest() throws Exception {
        //given
        final String path = "/auth/security/anonymous";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"anonymous\":true}";

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
    public static class AnonymousFallbackTestSecurityConfiguration {

        @Bean
        public SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder()
                    .handshake(serverWebExchange -> Mono.empty())
                    .fallbackToAnonymous(true)
                    .build(new ReactorNettyRequestUpgradeStrategy());
        }
    }

}
