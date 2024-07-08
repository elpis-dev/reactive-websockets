package org.elpis.reactive.websockets.impl.security;

import org.elpis.reactive.websockets.BaseWebSocketTest;
import org.elpis.reactive.websockets.context.BootStarter;
import org.elpis.reactive.websockets.context.resource.security.SecurityChainResource;
import org.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import org.elpis.reactive.websockets.context.security.model.TestConstants;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({SecurityChainTest.SecurityChainTestSecurityConfiguration.class, SecurityChainResource.class})
class SecurityChainTest extends BaseWebSocketTest {

    @Test
    void withExtractedAuthenticationValidChainTest() throws Exception {
        //given
        final HttpHeaders headers = new HttpHeaders();
        headers.add(TestConstants.PRINCIPAL, TestConstants.TEST_VALUE);

        final String path = "/auth/security/withExtractedAuthentication";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = TestConstants.TEST_VALUE;

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
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

    @Test
    void withExtractedAuthenticationChainValidationUnauthorizedTest(final CapturedOutput output) throws Exception {
        //given
        final HttpHeaders headers = new HttpHeaders();
        headers.add(TestConstants.PRINCIPAL, UUID.randomUUID().toString());

        final String path = "/auth/security/withExtractedAuthentication";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(sink::tryEmitValue)
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(output).contains("Invalid handshake response getStatus: 401 Unauthorized");
    }

    @TestConfiguration
    static class SecurityChainTestSecurityConfiguration {

        @Bean
        SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
            return http.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.anyExchange()
                    .access((authentication, context) -> {
                        final boolean hasValidHeader = Optional.ofNullable(context.getExchange().getRequest().getHeaders().get(TestConstants.PRINCIPAL))
                                .flatMap(headers -> headers.stream().findFirst())
                                .map(TestConstants.TEST_VALUE::equals)
                                .orElse(false);

                        return Mono.just(new AuthorizationDecision(hasValidHeader));
                    })).formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .build();
        }

        @Bean
        SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder()
                    .handshake(serverWebExchange -> Mono.justOrEmpty(Optional.ofNullable(serverWebExchange.getRequest().getHeaders().get(TestConstants.PRINCIPAL))
                            .flatMap(headers -> headers.stream().findFirst())))
                    .build(new ReactorNettyRequestUpgradeStrategy());
        }
    }

}
