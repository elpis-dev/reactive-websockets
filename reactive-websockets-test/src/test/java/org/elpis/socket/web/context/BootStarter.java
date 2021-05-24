package org.elpis.socket.web.context;

import org.elpis.reactive.websockets.EnableReactiveSocketSecurity;
import org.elpis.reactive.websockets.EnableReactiveSockets;
import org.elpis.reactive.websockets.config.annotations.impl.*;
import org.elpis.reactive.websockets.security.SocketHandshakeService;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.socket.web.context.resource.HeaderSocketResource;
import org.elpis.socket.web.context.resource.PathVariableSocketResource;
import org.elpis.socket.web.context.resource.QueryParamSocketResource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveSockets
@Import({
        HeaderSocketResource.class,
        PathVariableSocketResource.class,
        QueryParamSocketResource.class,
        AuthenticationAnnotationEvaluator.class,
        HeaderAnnotationEvaluator.class,
        MessageBodyAnnotationEvaluator.class,
        PathVariableAnnotationEvaluator.class,
        QueryParameterAnnotationEvaluator.class
})
public class BootStarter {
    public static void main(String[] args) {
        SpringApplication.run(BootStarter.class, args);
    }

    @EnableReactiveSocketSecurity
    public static class SecurityConfiguration {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
            return http.authorizeExchange().anyExchange().permitAll().and().build();
        }

        public static class SecurityHandshake extends SocketHandshakeService {

            public SecurityHandshake(final RequestUpgradeStrategy requestUpgradeStrategy) {
                super(requestUpgradeStrategy);
            }

            @Override
            public Mono<?> handshake(@NonNull ServerWebExchange exchange) {
                return Mono.just(new Anonymous());
            }

        }
    }

    public enum Example {
        VOID
    }
}
