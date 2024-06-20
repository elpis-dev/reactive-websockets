package org.elpis.reactive.socket.web.impl.security;

import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.reactive.socket.web.BaseWebSocketTest;
import org.elpis.reactive.socket.web.context.BootStarter;
import org.elpis.reactive.socket.web.context.resource.security.WebFilterSecurityResource;
import org.elpis.reactive.socket.web.context.security.model.SecurityProfiles;
import org.elpis.reactive.socket.web.context.security.model.TestConstants;
import org.elpis.reactive.socket.web.context.security.model.TestPrincipal;
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
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BootStarter.class)
@ActiveProfiles({BaseWebSocketTest.DEFAULT_TEST_PROFILE, SecurityProfiles.FULL})
@Import({WebFilterAuthenticationSocketTest.WebFilterAuthorizationTestSecurityConfiguration.class, WebFilterSecurityResource.class})
public class WebFilterAuthenticationSocketTest extends BaseWebSocketTest {

    @Test
    public void withPrincipalTest() throws Exception {
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
    public void withAuthenticationTest() throws Exception {
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
    public void withDetailsFromAuthentication() throws Exception {
        //given
        final String path = "/auth/filter/withDetailsFromAuthentication";
        final Sinks.One<String> sink = Sinks.one();

        //expected
        final String expected = TestConstants.TEST_VALUE;

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
    public static class WebFilterAuthorizationTestSecurityConfiguration {

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
                    .build();
        }
    }

}
