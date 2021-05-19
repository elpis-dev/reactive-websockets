package org.elpis.reactive.socket.security;

import org.elpis.reactive.socket.security.principal.Anonymous;
import org.elpis.reactive.socket.security.principal.WebSocketPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

public abstract class SocketHandshakeService extends HandshakeWebSocketService {
    private static final Logger LOG = LoggerFactory.getLogger(SocketHandshakeService.class);

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

    public SocketHandshakeService(final RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    public Mono<ServerResponse> handleError(@NonNull final Throwable throwable) {
        LOG.error(throwable.getMessage(), throwable);

        return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
    }

    public Mono<?> handshake(@NonNull final ServerWebExchange exchange) {
        return Mono.just(new Anonymous());
    }

    @Override
    @NonNull
    public Mono<Void> handleRequest(@NonNull final ServerWebExchange exchange, @NonNull final WebSocketHandler handler) {
        return this.handshake(exchange)
                .onErrorResume(throwable -> this.handleError(throwable)
                        .flatMap(serverResponse -> serverResponse.writeTo(exchange, EMPTY_CONTEXT))
                        .then(Mono.empty()))
                .map(credentials -> Principal.class.isAssignableFrom(credentials.getClass())
                        ? (Principal) credentials
                        : new WebSocketPrincipal<>(credentials))
                .map(principal -> exchange.mutate().principal(Mono.just(principal)).build())
                .flatMap(request -> super.handleRequest(request, handler));
    }
}
