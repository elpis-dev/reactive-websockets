package org.elpis.socket.web.impl.security;

import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.socket.web.BaseWebSocketTest;
import org.elpis.socket.web.context.BootStarter;
import org.elpis.socket.web.context.resource.security.SecurityChainResource;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.elpis.socket.web.context.security.model.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.socket.WebSocketMessage;
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
@Import({ExchangeMatcherTest.CustomExchangeMatherTestSecurityConfiguration.class, SecurityChainResource.class})
@TestPropertySource("classpath:application-test-disabled-default-security.properties")
public class ExchangeMatcherTest extends BaseWebSocketTest {

    @Test
    public void withExtractedAuthenticationValidChainTest() throws Exception {
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
    public void withExtractedAuthenticationFailedChainValidationTest(final CapturedOutput output) throws Exception {
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


    @Test
    public void withWrongAuthenticationObjectTest(final CapturedOutput output) throws Exception {
        //given
        final HttpHeaders headers = new HttpHeaders();
        headers.add(TestConstants.PRINCIPAL, TestConstants.TEST_VALUE);

        final String path = "/auth/security/falseAuthenticationInstance";
        final Sinks.One<String> sink = Sinks.one();

        //test
        this.withClient(path, headers, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(sink::tryEmitValue)
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono().timeout(DEFAULT_FAST_TEST_FALLBACK))
                .verifyError(TimeoutException.class);

        assertThat(output).contains("Unable register method `withExtractedAuthentication()`. Requested @SocketAuthentication type: java.lang.Void, found: java.lang.String");
    }

    @TestConfiguration
    public static class CustomExchangeMatherTestSecurityConfiguration {

        @Bean
        public SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder()
                    .exchangeMatcher(this::serverWebExchangeMatcher)
                    .handshake(serverWebExchange -> Mono.justOrEmpty(Optional.ofNullable(serverWebExchange.getRequest().getHeaders().get(TestConstants.PRINCIPAL))
                            .flatMap(headers -> headers.stream().findFirst())))
                    .build();
        }

        private ServerWebExchangeMatcher serverWebExchangeMatcher() {
            return exchange -> {
                final boolean hasValidHeader = Optional.ofNullable(exchange.getRequest().getHeaders().get(TestConstants.PRINCIPAL))
                        .flatMap(headers -> headers.stream().findFirst())
                        .map(TestConstants.TEST_VALUE::equals)
                        .orElse(false);

                return hasValidHeader
                        ? ServerWebExchangeMatcher.MatchResult.match()
                        : ServerWebExchangeMatcher.MatchResult.notMatch();
            };
        }
    }

}
