package org.elpis.socket.web.context.security;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.socket.web.context.security.model.SecurityProfiles;
import org.elpis.socket.web.context.security.model.TestConstants;
import org.elpis.socket.web.context.security.model.TestPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@EnableReactiveSocketSecurity
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile(SecurityProfiles.GENERIC)
public class TestSecurityConfiguration {

    @Configuration
    @Profile(SecurityProfiles.WEB_FILTER_AUTHORIZATION)
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
                final Authentication authentication = new AnonymousAuthenticationToken(SecurityProfiles.GENERIC, new TestPrincipal(),
                        List.of(new SimpleGrantedAuthority(SecurityProfiles.GENERIC)));

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

    @Configuration
    @Profile(SecurityProfiles.WEB_FILTER_ATTRIBUTE)
    public static class WebFilterAttributeTestSecurityConfiguration {

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
            final WebFilter webFilter = (exchange, chain) -> {
                exchange.getAttributes().put(TestConstants.PRINCIPAL, TestConstants.TEST_VALUE);
                return chain.filter(exchange);
            };

            return SocketHandshakeService.builder()
                    .handshake(webFilter::filter, serverWebExchange -> Mono
                            .justOrEmpty(Optional.ofNullable(serverWebExchange.getAttribute(TestConstants.PRINCIPAL)))).build();
        }
    }


    @Configuration
    @Profile(SecurityProfiles.SECURITY_CHAIN)
    public static class SecurityChainTestSecurityConfiguration {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
            return http.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.anyExchange()
                    .access((authentication, context) -> {
                        final boolean hasValidHeader = Optional.ofNullable(context.getExchange().getRequest().getHeaders().get(TestConstants.PRINCIPAL))
                                .flatMap(headers -> headers.stream().findFirst())
                                .map(TestConstants.TEST_VALUE::equals)
                                .orElse(false);

                        return Mono.just(new AuthorizationDecision(hasValidHeader));
                    })).formLogin().disable().build();
        }

        @Bean
        public SocketHandshakeService socketHandshakeService() {
            return SocketHandshakeService.builder()
                    .handshake(serverWebExchange -> Mono.justOrEmpty(Optional.ofNullable(serverWebExchange.getRequest().getHeaders().get(TestConstants.PRINCIPAL))
                            .flatMap(headers -> headers.stream().findFirst())))
                    .build();
        }
    }

    @Configuration
    @PropertySource("classpath:application-test-disabled-default-security.properties")
    @Profile(SecurityProfiles.CUSTOM_EXCHANGE_MATCHER)
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
