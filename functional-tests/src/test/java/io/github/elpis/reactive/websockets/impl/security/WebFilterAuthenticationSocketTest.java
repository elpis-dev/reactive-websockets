package io.github.elpis.reactive.websockets.impl.security;

import io.github.elpis.reactive.websockets.BaseWebSocketTest;
import io.github.elpis.reactive.websockets.context.BootStarter;
import io.github.elpis.reactive.websockets.context.resource.security.WebFilterSecurityResource;
import io.github.elpis.reactive.websockets.context.security.model.SecurityProfiles;
import io.github.elpis.reactive.websockets.context.security.model.TestConstants;
import io.github.elpis.reactive.websockets.context.security.model.TestPrincipal;
import io.github.elpis.reactive.websockets.security.SocketHandshakeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({WebFilterAuthenticationSocketTest.WebFilterAuthorizationTestSecurityConfiguration.class, WebFilterSecurityResource.class})
class WebFilterAuthenticationSocketTest extends BaseWebSocketTest {

    @Test
    void withPrincipalTest() throws Exception {
        //given
        final String path = "/auth/filter/withPrincipal";
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

    @Test
    void withAuthenticationTest() throws Exception {
        //given
        final String path = "/auth/filter/withAuthentication";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = "{\"principal\":\"" + TestPrincipal.class.getName() + "\"}";

        //test
        this.withClient(path, (session) -> session.receive().map(WebSocketMessage::getPayloadAsText)
                .log()
                .doOnNext(v -> sink.tryEmitValue(v.replaceAll(" ", "")))
                .then()).subscribe();

        //verify
        StepVerifier.create(sink.asMono())
                .expectNext(expected)
                .expectComplete()
                .log()
                .verify(DEFAULT_GENERIC_TEST_FALLBACK);
    }

    @Test
    void withExpressionPrincipalTest() throws Exception {
        //given
        final String path = "/auth/filter/withExpressionPrincipal";
        final Sinks.One<String> sink = Sinks.one();

        //expected - the expression extracts the principal's name as a String
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
    static class WebFilterAuthorizationTestSecurityConfiguration {

        @Bean
        SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
            return http.authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .build();
        }

        @Bean
        SocketHandshakeService socketHandshakeService() {
            final WebFilter webFilter = (exchange, chain) -> ReactiveSecurityContextHolder.getContext().switchIfEmpty(Mono.defer(() -> {
                final AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(TestConstants.TEST_VALUE, new TestPrincipal(),
                        List.of(new SimpleGrantedAuthority(TestConstants.TEST_VALUE)));
                authentication.setDetails(TestConstants.TEST_VALUE);

                final SecurityContext securityContext = new SecurityContextImpl(authentication);
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                        .then(Mono.empty());
            })).flatMap((securityContext) -> chain.filter(exchange));

            return SocketHandshakeService.builder()
                    .handshake(webFilter::filter)
                    .build(new ReactorNettyRequestUpgradeStrategy());
        }
    }

}
