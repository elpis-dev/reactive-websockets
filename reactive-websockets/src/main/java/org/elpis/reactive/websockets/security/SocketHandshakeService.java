package org.elpis.reactive.websockets.security;

import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.utils.TriFunction;
import org.elpis.reactive.websockets.web.model.WebSocketError;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.codec.Encoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.ArrayList;
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
            final Encoder<?> encoder = new Jackson2JsonEncoder();
            return List.of(new EncoderHttpMessageWriter<>(encoder));
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
        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new WebSocketError(throwable.getMessage()));
    }

    public Mono<?> handshake(@lombok.NonNull final ServerWebExchange exchange) {
        return Mono.just(new Anonymous());
    }

    public SecurityWebFilterChain securityChain() {
        return this.getHttp().build();
    }

    @Override
    @NonNull
    public Mono<Void> handleRequest(@NotNull @lombok.NonNull final ServerWebExchange exchange,
                                    @NotNull @lombok.NonNull final WebSocketHandler handler) {

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
        private Function<Throwable, Mono<ServerResponse>> errorHandler;

        private BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter;
        private Function<ServerWebExchange, Mono<?>> handshakeWithServerWebExchange;

        private Supplier<SecurityWebFilterChain> securityChain;
        private Function<ServerHttpSecurity, SecurityWebFilterChain> securityChainWithServerHttpSecurity;

        private TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> requestHandler;

        private RequestUpgradeStrategy requestUpgradeStrategy;

        private Builder() {

        }

        public Builder handleError(Function<Throwable, Mono<ServerResponse>> errorHandler) {
            this.errorHandler = errorHandler;

            return this;
        }

        public Builder handshake(BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter) {
            this.handshakeWithWebFilter = handshakeWithWebFilter;

            return this;
        }

        public Builder handshake(Function<ServerWebExchange, Mono<?>> handshakeWithServerWebExchange) {
            this.handshakeWithServerWebExchange = handshakeWithServerWebExchange;

            return this;
        }

        public Builder securityChain(Supplier<SecurityWebFilterChain> securityChain) {
            this.securityChain = securityChain;

            return this;
        }

        public Builder securityChain(Function<ServerHttpSecurity, SecurityWebFilterChain> securityChainWithServerHttpSecurity) {
            this.securityChainWithServerHttpSecurity = securityChainWithServerHttpSecurity;

            return this;
        }

        public Builder handleRequest(TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> handleRequest) {
            this.requestHandler = handleRequest;

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
                    return Optional.ofNullable(errorHandler)
                            .map(func -> func.apply(throwable))
                            .orElseGet(() -> super.handleError(throwable));
                }

                @Override
                public Mono<?> handshake(@lombok.NonNull ServerWebExchange exchange) {
                    if (nonNull(handshakeWithWebFilter)) {
                        final List<Principal> principals = new ArrayList<>();
                        final Flux<Principal> flux = Flux.fromIterable(principals);

                        return handshakeWithWebFilter.apply(exchange, serverWebExchange -> serverWebExchange.getPrincipal()
                                .switchIfEmpty(Mono.error(() -> new HttpServerErrorException(HttpStatus.UNAUTHORIZED, "Cannot get a Principal from request")))
                                .onErrorResume(throwable -> this.handleError(throwable)
                                        .flatMap(serverResponse -> serverResponse.writeTo(exchange, EMPTY_CONTEXT))
                                        .then(Mono.empty()))
                                .doOnNext(principals::add).then())
                                .then(flux.next());
                    } else if (nonNull(handshakeWithServerWebExchange)) {
                        return handshakeWithServerWebExchange.apply(exchange);
                    } else {
                        return super.handshake(exchange);
                    }
                }

                @Override
                public SecurityWebFilterChain securityChain() {
                    if (nonNull(securityChain)) {
                        return securityChain.get();
                    } else if (nonNull(securityChainWithServerHttpSecurity)) {
                        return securityChainWithServerHttpSecurity.apply(this.getHttp());
                    } else {
                        return super.securityChain();
                    }
                }

                @NotNull
                @Override
                public Mono<Void> handleRequest(@NotNull @lombok.NonNull ServerWebExchange exchange, @NotNull @lombok.NonNull WebSocketHandler handler) {
                    return Optional.ofNullable(requestHandler)
                            .map(func -> func.apply(this, exchange, handler))
                            .orElseGet(() -> super.handleRequest(exchange, handler));
                }
            };
        }
    }
}
