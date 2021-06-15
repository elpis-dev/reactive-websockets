package org.elpis.reactive.websockets.security;

import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.utils.TriFunction;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

public abstract class SocketHandshakeService extends HandshakeWebSocketService {
    public static final ServerResponse.Context EMPTY_CONTEXT = new ServerResponse.Context() {

        @Override
        @NonNull
        public List<HttpMessageWriter<?>> messageWriters() {
            return List.of();
        }

        @Override
        @NonNull
        public List<ViewResolver> viewResolvers() {
            return List.of();
        }

    };

    private final ServerHttpSecurity http = ServerHttpSecurity.http();

    public SocketHandshakeService(final RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    public Mono<ServerResponse> handleError(@lombok.NonNull final Throwable throwable) {
        return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(throwable.getMessage());
    }

    public Mono<?> handshake(@lombok.NonNull final ServerWebExchange exchange) {
        return Mono.just(new Anonymous());
    }

    public SecurityWebFilterChain securityChain() {
        return this.getHttp().build();
    }

    @Override
    @NonNull
    public Mono<Void> handleRequest(@lombok.NonNull final ServerWebExchange exchange, @lombok.NonNull final WebSocketHandler handler) {
        return this.securityChain().matches(exchange)
                .flatMap(matches -> matches
                        ? this.handshake(exchange)
                        : Mono.error(() -> new HttpServerErrorException(HttpStatus.UNAUTHORIZED, "Security chain failed")))
                .onErrorResume(throwable -> this.handleError(throwable)
                        .flatMap(serverResponse -> serverResponse.writeTo(exchange, EMPTY_CONTEXT))
                        .then(Mono.empty()))
                .map(credentials -> Principal.class.isAssignableFrom(credentials.getClass())
                        ? (Principal) credentials
                        : new WebSocketPrincipal<>(credentials))
                .map(principal -> exchange.mutate().principal(Mono.just(principal)).build())
                .flatMap(request -> super.handleRequest(request, handler));
    }

    public ServerHttpSecurity getHttp() {
        return http;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Function<Throwable, Mono<ServerResponse>> handleError;
        private BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshake;
        private Function<ServerWebExchange, Mono<?>> handshakeSingle;

        private Supplier<SecurityWebFilterChain> securityChainSupplier;
        private Function<ServerHttpSecurity, SecurityWebFilterChain> securityChainFunction;

        private TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> handleRequest;

        private RequestUpgradeStrategy requestUpgradeStrategy;

        private Builder() {

        }

        public Builder handleError(Function<Throwable, Mono<ServerResponse>> handleError) {
            this.handleError = handleError;

            return this;
        }

        public Builder handshake(BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshake) {
            this.handshake = handshake;

            return this;
        }

        public Builder handshake(Function<ServerWebExchange, Mono<?>> handshake) {
            this.handshakeSingle = handshake;

            return this;
        }

        public Builder securityChain(Supplier<SecurityWebFilterChain> securityChainSupplier) {
            this.securityChainSupplier = securityChainSupplier;

            return this;
        }

        public Builder securityChain(Function<ServerHttpSecurity, SecurityWebFilterChain> securityChainFunction) {
            this.securityChainFunction = securityChainFunction;

            return this;
        }

        public Builder handleRequest(TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> handleRequest) {
            this.handleRequest = handleRequest;

            return this;
        }

        public Builder requestUpgradeStrategy(RequestUpgradeStrategy requestUpgradeStrategy) {
            this.requestUpgradeStrategy = requestUpgradeStrategy;

            return this;
        }

        public SocketHandshakeService build() {
            return new SocketHandshakeService(Optional.ofNullable(this.requestUpgradeStrategy)
                    .orElseGet(ReactorNettyRequestUpgradeStrategy::new)) {

                @Override
                public Mono<ServerResponse> handleError(@lombok.NonNull Throwable throwable) {
                    return Optional.ofNullable(handleError)
                            .map(func -> func.apply(throwable))
                            .orElseGet(() -> super.handleError(throwable));
                }

                @Override
                public Mono<?> handshake(@lombok.NonNull ServerWebExchange exchange) {
                    if (nonNull(handshake)) {
                        return handshake.apply(exchange, serverWebExchange -> this.securityChain().matches(serverWebExchange)
                                .filter(matches -> matches)
                                .onErrorResume(throwable -> this.handleError(throwable)
                                        .flatMap(serverResponse -> serverResponse.writeTo(exchange, EMPTY_CONTEXT))
                                        .then(Mono.empty()))
                                .switchIfEmpty(Mono.error(() -> new HttpServerErrorException(HttpStatus.UNAUTHORIZED, "Security chain failed")))
                                .then());
                    } else if (nonNull(handshakeSingle)) {
                        return handshakeSingle.apply(exchange);
                    } else {
                        return super.handshake(exchange);
                    }
                }

                @Override
                public SecurityWebFilterChain securityChain() {
                    if (nonNull(securityChainSupplier)) {
                        return securityChainSupplier.get();
                    } else if (nonNull(securityChainFunction)) {
                        return securityChainFunction.apply(this.getHttp());
                    } else {
                        return super.securityChain();
                    }
                }

                @Override
                public Mono<Void> handleRequest(@lombok.NonNull ServerWebExchange exchange, @lombok.NonNull WebSocketHandler handler) {
                    return Optional.ofNullable(handleRequest)
                            .map(func -> func.apply(this, exchange, handler))
                            .orElseGet(() -> super.handleRequest(exchange, handler));
                }
            };
        }
    }
}
