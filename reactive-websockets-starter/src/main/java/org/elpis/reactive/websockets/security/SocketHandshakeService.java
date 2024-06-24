package org.elpis.reactive.websockets.security;

import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import org.elpis.reactive.websockets.security.principal.Anonymous;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.elpis.reactive.websockets.util.TriFunction;
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

/**
 * Abstract implementation of {@link HandshakeWebSocketService} to support generic handshake process.
 *
 * @author Alex Zharkov
 * @see HandshakeWebSocketService
 * @since 0.1.0
 */
public abstract class SocketHandshakeService extends HandshakeWebSocketService {
    protected SocketHandshakeService(final RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    /**
     * Provides custom {@link WebExceptionHandler} to process exceptions and server response when handshake.
     *
     * @return any {@link WebExceptionHandler} implementation.
     * @since 0.1.0
     */
    public WebExceptionHandler errorHandler() {
        return new ResponseStatusExceptionHandler();
    }

    /**
     * Provides custom authentication object: either any {@link Principal} or {@link org.springframework.security.core.Authentication} or any {@link Object}.
     * <p><strong>NOTE: </strong>Any {@link Object} except {@link Principal} or {@link org.springframework.security.core.Authentication} will be wrapped with {@link WebSocketPrincipal}.
     * <pre>
     * public Mono<?> handshake(final ServerWebExchange exchange) {
     *    return exchange.getPrincipal();
     * }
     *
     * // OR
     *
     * public Mono<?> handshake(final ServerWebExchange exchange) {
     *    return Mono.just(...);
     * }
     *
     * // OR
     *
     * public Mono<?> handshake(final ServerWebExchange exchange) {
     *    final String token = exchange.getRequest().getHeaders().get("Authorization").get(0);
     *    return authenticationService.authenticate(token);
     * }
     * </pre>
     *
     * @param exchange server exchange instance
     * @return {@link Mono}
     * @since 0.1.0
     */
    public Mono<?> handshake(final ServerWebExchange exchange) {
        return Mono.just(new Anonymous());
    }

    /**
     * Provides custom security, request ot handshake validation.
     * <pre>
     * public ServerWebExchangeMatcher exchangeMatcher() {
     *    return exchange -> {
     *       final String token = exchange.getRequest().getHeaders().get("Authorization").get(0);
     *
     *       final boolean hasValidHeader = Optional.ofNullable(token)
     *               .flatMap(headers -> headers.stream().findFirst())
     *               .map(header -> header.contains("Bearer"))
     *               .orElse(false);
     *
     *        return hasValidHeader
     *           ? ServerWebExchangeMatcher.MatchResult.match()
     *           : ServerWebExchangeMatcher.MatchResult.notMatch();
     *    };
     * }
     * </pre>
     *
     * @return any {@link ServerWebExchangeMatcher} implementation
     * @since 0.1.0
     */
    public ServerWebExchangeMatcher exchangeMatcher() {
        return ServerWebExchangeMatchers.anyExchange();
    }

    /**
     * Abstract method {@link org.springframework.web.reactive.socket.server.WebSocketService#handleRequest(ServerWebExchange, WebSocketHandler)} with pre-defined flow:
     * <ul>
     *    <li>Calling {@link #exchangeMatcher()} to validate exchange before processing
     *    <ul>
     *      <li>If {@link ServerWebExchangeMatcher.MatchResult#isMatch()} is false - throwing a {@link ResponseStatusException ResponseStatusException(HttpStatus.UNAUTHORIZED)}
     *    </ul>
     *    <li>Calling {@link #handshake(ServerWebExchange)} to grab a principal
     *    <ul>
     *      <li>If {@link Mono#empty()} would be returned - will default to creating a {@link Anonymous} principal
     *    </ul>
     *    <li>Calling {@link ServerWebExchange#mutate()} to push a returned principal - handshake successful
     * </ul>
     *
     * @return {@link Mono Mono<Void>}
     * @since 0.1.0
     */
    @Override
    public Mono<Void> handleRequest(final ServerWebExchange exchange, final WebSocketHandler handler) {
        return Mono.just(exchange)
                .filterWhen(serverWebExchange -> this.exchangeMatcher().matches(serverWebExchange).map(ServerWebExchangeMatcher.MatchResult::isMatch))
                .switchIfEmpty(Mono.error(() -> new WebSocketClientHandshakeException("Security chain failed")))
                .onErrorResume(throwable -> this.errorHandler().handle(exchange, throwable).then(Mono.empty()))
                .flatMap(serverWebExchange -> this.handshake(serverWebExchange).switchIfEmpty(Mono.just(this.cast(new Anonymous()))))
                .map(credentials -> Principal.class.isAssignableFrom(credentials.getClass())
                        ? (Principal) credentials
                        : new WebSocketPrincipal<>(credentials))
                .map(principal -> exchange.mutate().principal(Mono.just(principal)).build())
                .flatMap(request -> super.handleRequest(request, handler));
    }

    /**
     * Creates new {@link Builder} instance.
     *
     * @return new {@link Builder}
     * @since 0.1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Supplier<WebExceptionHandler> errorHandler;

        private BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter;
        private Function<ServerWebExchange, Mono<?>> handshakeWithServerWebExchange;
        private Function<ServerWebExchange, Mono<?>> principalExtractor;

        private ServerWebExchangeMatcher exchangeMatcher;

        private TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> requestHandler;

        private RequestUpgradeStrategy requestUpgradeStrategy;

        private boolean fallbackToAnonymous = false;

        private Builder() {
            // Hiding builder
        }

        /**
         * Base for {@link SocketHandshakeService#errorHandler()}.
         *
         * @param errorHandler the {@link WebExceptionHandler} supplier
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder errorHandler(Supplier<WebExceptionHandler> errorHandler) {
            this.errorHandler = errorHandler;

            return this;
        }

        /**
         * Base for {@link SocketHandshakeService#handshake(ServerWebExchange)} that also accepts a {@link WebFilterChain}. If set - {@link #handshake(Function)} would be ignored.
         * <pre>
         * WebFilter webFilter = new WebFilter();
         * SocketHandshakeService.builder()
         *    .handshake(webFilter::filter)
         *    .build()
         * </pre>
         *
         * @param handshakeWithWebFilter function that processes request with {@link WebFilterChain}
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder handshake(BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter) {
            this.handshakeWithWebFilter = handshakeWithWebFilter;
            this.principalExtractor = null;

            return this;
        }

        public Builder fallbackToAnonymous(boolean fallbackToAnonymous) {
            this.fallbackToAnonymous = fallbackToAnonymous;

            return this;
        }

        /**
         * Base for {@link SocketHandshakeService#handshake(ServerWebExchange)} that also accepts a {@link WebFilterChain} and provides a {@link Function} that gets a principal. If set - {@link #handshake(Function)} would be ignored.
         * <pre>
         * final String token = exchange.getRequest().getHeaders().get("Authorization").get(0);
         * final WebFilter webFilter = new WebFilter();
         * SocketHandshakeService.builder()
         *    .handshake(webFilter::filter, exchange -> authenticationService.authenticate(token))
         *    .build()
         * </pre>
         *
         * @param handshakeWithWebFilter function that processes request with {@link WebFilterChain}
         * @param principalExtractor     function that fetches a principal object from request
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder handshake(BiFunction<ServerWebExchange, WebFilterChain, Mono<?>> handshakeWithWebFilter,
                                 Function<ServerWebExchange, Mono<?>> principalExtractor) {

            this.handshakeWithWebFilter = handshakeWithWebFilter;
            this.principalExtractor = principalExtractor;

            return this;
        }

        /**
         * Base for {@link SocketHandshakeService#handshake(ServerWebExchange)} based only on {@link ServerWebExchange}. Ignored if {@link #handshake(BiFunction, Function)} or {@link #handshake(BiFunction)} is set.
         * <pre>
         * WebFilter webFilter = new WebFilter();
         * SocketHandshakeService.builder()
         *    .handshake(webFilter::filter)
         *    .build()
         * </pre>
         *
         * @param handshakeWithServerWebExchange function that processes request
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder handshake(Function<ServerWebExchange, Mono<?>> handshakeWithServerWebExchange) {
            this.handshakeWithServerWebExchange = handshakeWithServerWebExchange;

            return this;
        }

        /**
         * Base for {@link SocketHandshakeService#exchangeMatcher()}.
         * <pre>
         * ...
         * .exchangeMatcher(exchange -> ServerWebExchangeMatcher.MatchResult.match())
         * ...
         * </pre>
         *
         * @param exchangeMatcher {@link ServerWebExchangeMatcher}
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder exchangeMatcher(ServerWebExchangeMatcher exchangeMatcher) {
            this.exchangeMatcher = exchangeMatcher;

            return this;
        }

        /**
         * Base for {@link SocketHandshakeService#handleRequest(ServerWebExchange, WebSocketHandler)}.
         *
         * @param handleRequest {@link TriFunction} synonym of {@link SocketHandshakeService#handleRequest(ServerWebExchange, WebSocketHandler)}
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder handleRequest(TriFunction<SocketHandshakeService, ServerWebExchange, WebSocketHandler, Mono<Void>> handleRequest) {
            this.requestHandler = handleRequest;

            return this;
        }

        /**
         * Base for {@link SocketHandshakeService#SocketHandshakeService(RequestUpgradeStrategy)}.
         *
         * @param requestUpgradeStrategy any {@link RequestUpgradeStrategy} implementation
         * @return {@link Builder}
         * @since 0.1.0
         */
        public Builder requestUpgradeStrategy(RequestUpgradeStrategy requestUpgradeStrategy) {
            this.requestUpgradeStrategy = requestUpgradeStrategy;

            return this;
        }

        /**
         * Main build method for {@link Builder}.
         * <p><strong>NOTE: </strong> if no replace function is set for any of:
         * <ul>
         *     <li>{@link SocketHandshakeService#exchangeMatcher()}
         *     <li>{@link SocketHandshakeService#handshake(ServerWebExchange)}
         *     <li>{@link SocketHandshakeService#handleRequest(ServerWebExchange, WebSocketHandler)}
         *     <li>{@link SocketHandshakeService#errorHandler()}
         * </ul>
         * - version from {@code super} will be used.
         * <p>For {@link #requestUpgradeStrategy(RequestUpgradeStrategy)} - will default to {@code new ReactorNettyRequestUpgradeStrategy()}.
         *
         * @return {@link SocketHandshakeService}
         * @since 0.1.0
         */
        public SocketHandshakeService build() {
            return new SocketHandshakeService(Optional.ofNullable(this.requestUpgradeStrategy)
                    .orElseGet(ReactorNettyRequestUpgradeStrategy::new)) {

                private final Function<ServerWebExchange, Mono<?>> exchangeProcessor = serverWebExchange -> nonNull(principalExtractor)
                        ? principalExtractor.apply(serverWebExchange)
                        : serverWebExchange.getPrincipal();

                @Override
                public WebExceptionHandler errorHandler() {
                    return Optional.ofNullable(errorHandler).map(Supplier::get).orElseGet(super::errorHandler);
                }

                @Override
                public Mono<?> handshake(ServerWebExchange exchange) {
                    if (nonNull(handshakeWithWebFilter)) {
                        final List<Object> principals = new ArrayList<>();
                        final Flux<?> flux = Flux.fromIterable(principals);

                        return handshakeWithWebFilter.apply(exchange, serverWebExchange -> exchangeProcessor.apply(serverWebExchange)
                                        .switchIfEmpty(fallbackToAnonymous
                                                ? Mono.just(cast(new Anonymous()))
                                                : Mono.error(() -> new WebSocketClientHandshakeException("Cannot resolve Principal from handshake")))
                                        .onErrorResume(throwable -> this.errorHandler().handle(exchange, throwable).then(Mono.empty()))
                                        .doOnNext(principals::add).then())
                                .then(flux.next());
                    } else if (nonNull(handshakeWithServerWebExchange)) {
                        return handshakeWithServerWebExchange.apply(exchange)
                                .switchIfEmpty(fallbackToAnonymous
                                        ? Mono.just(cast(new Anonymous()))
                                        : Mono.error(() -> new WebSocketClientHandshakeException("Cannot resolve Principal from handshake")))
                                .onErrorResume(throwable -> this.errorHandler().handle(exchange, throwable).then(Mono.empty()));
                    } else {
                        return super.handshake(exchange);
                    }
                }

                @Override
                public ServerWebExchangeMatcher exchangeMatcher() {
                    return nonNull(exchangeMatcher) ? exchangeMatcher : super.exchangeMatcher();
                }

                @Override
                public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
                    return Optional.ofNullable(requestHandler)
                            .map(func -> func.apply(this, exchange, handler))
                            .orElseGet(() -> super.handleRequest(exchange, handler));
                }
            };
        }
    }

    <T> T cast(Object o) {
        return (T) o;
    }
}
