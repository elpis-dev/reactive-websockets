package org.elpis.reactive.websockets.security;

import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.util.TriFunction;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
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
    public SocketHandshakeService(final RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    @lombok.NonNull
    public WebExceptionHandler errorHandler() {
        return new ResponseStatusExceptionHandler();
    }

    @lombok.NonNull
    public Mono<?> handshake(@lombok.NonNull final ServerWebExchange exchange) {
        return Mono.just(new Anonymous());
    }

    @lombok.NonNull
    public ServerWebExchangeMatcher exchangeMatcher() {
        return ServerWebExchangeMatchers.anyExchange();
    }

    @Override
    @lombok.NonNull
    public Mono<Void> handleRequest(@lombok.NonNull final ServerWebExchange exchange,
                                    @lombok.NonNull final WebSocketHandler handler) {

        return Mono.just(exchange)
                .filterWhen(serverWebExchange -> this.exchangeMatcher().matches(serverWebExchange).map(ServerWebExchangeMatcher.MatchResult::isMatch))
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Security chain failed")))
                .onErrorResume(throwable -> this.errorHandler().handle(exchange, throwable).then(Mono.empty()))
                .flatMap(this::handshake)
                .map(credentials -> Principal.class.isAssignableFrom(credentials.getClass())
                        ? (Principal) credentials
                        : new WebSocketPrincipal<>(credentials))
                .map(principal -> exchange.mutate().principal(Mono.just(principal)).build())
                .flatMap(request -> super.handleRequest(request, handler));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Supplier<WebExceptionHandler> errorHandler;

        private BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter;
        private Function<ServerWebExchange, Mono<?>> handshakeWithServerWebExchange;

        private Function<ServerWebExchange, Mono<?>> principalExtractor;

        private Supplier<ServerWebExchangeMatcher> exchangeMatcher;

        private TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> requestHandler;

        private RequestUpgradeStrategy requestUpgradeStrategy;

        private Builder() {
            // Hiding builder
        }

        public Builder errorHandler(Supplier<WebExceptionHandler> errorHandler) {
            this.errorHandler = errorHandler;

            return this;
        }

        public Builder handshake(BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter) {
            this.handshakeWithWebFilter = handshakeWithWebFilter;
            this.principalExtractor = null;

            return this;
        }

        public Builder handshake(BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter,
                                 Function<ServerWebExchange, Mono<?>> principalExtractor) {

            this.handshakeWithWebFilter = handshakeWithWebFilter;
            this.principalExtractor = principalExtractor;

            return this;
        }

        public Builder handshake(Function<ServerWebExchange, Mono<?>> handshakeWithServerWebExchange) {
            this.handshakeWithServerWebExchange = handshakeWithServerWebExchange;

            return this;
        }

        public Builder exchangeMatcher(Supplier<ServerWebExchangeMatcher> exchangeMatcher) {
            this.exchangeMatcher = exchangeMatcher;

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

                private final Function<ServerWebExchange, Mono<?>> exchangeProcessor = serverWebExchange -> nonNull(principalExtractor)
                        ? principalExtractor.apply(serverWebExchange)
                        : serverWebExchange.getPrincipal();

                @Override
                @lombok.NonNull
                public WebExceptionHandler errorHandler() {
                    return Optional.ofNullable(errorHandler).map(Supplier::get).orElseGet(super::errorHandler);
                }

                @Override
                @lombok.NonNull
                public Mono<?> handshake(@lombok.NonNull ServerWebExchange exchange) {
                    if (nonNull(handshakeWithWebFilter)) {
                        final List<Object> principals = new ArrayList<>();
                        final Flux<?> flux = Flux.fromIterable(principals);

                        return handshakeWithWebFilter.apply(exchange, serverWebExchange -> exchangeProcessor.apply(serverWebExchange)
                                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot get a Principal from request")))
                                .onErrorResume(throwable -> this.errorHandler().handle(exchange, throwable).then(Mono.empty()))
                                .doOnNext(principals::add).then())
                                .then(flux.next());
                    } else if (nonNull(handshakeWithServerWebExchange)) {
                        return handshakeWithServerWebExchange.apply(exchange)
                                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot get a Principal from request")))
                                .onErrorResume(throwable -> this.errorHandler().handle(exchange, throwable).then(Mono.empty()));
                    } else {
                        return super.handshake(exchange);
                    }
                }

                @Override
                @lombok.NonNull
                public ServerWebExchangeMatcher exchangeMatcher() {
                    return nonNull(exchangeMatcher)
                            ? exchangeMatcher.get()
                            : super.exchangeMatcher();
                }

                @Override
                @lombok.NonNull
                public Mono<Void> handleRequest(@lombok.NonNull ServerWebExchange exchange, @lombok.NonNull WebSocketHandler handler) {
                    return Optional.ofNullable(requestHandler)
                            .map(func -> func.apply(this, exchange, handler))
                            .orElseGet(() -> super.handleRequest(exchange, handler));
                }
            };
        }
    }
}
