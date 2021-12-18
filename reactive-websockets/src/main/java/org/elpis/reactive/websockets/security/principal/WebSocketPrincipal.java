package org.elpis.reactive.websockets.security.principal;

import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;

import java.io.Serializable;
import java.security.Principal;

/**
 * {@link Principal Principal}-wrapper class that puts a custom {@link Object} into security context.
 * Usually will wrap any returned from {@link org.elpis.reactive.websockets.security.SocketHandshakeService#handshake(ServerWebExchange)}.
 *
 *<pre class="code">
 *public Mono<?> handshake(final ServerWebExchange exchange) {
 *   return Mono.just(new MyCustomObject());
 *}
 *</pre>
 *
 * @author Alex Zharkov
 * @see Principal
 * @see org.elpis.reactive.websockets.security.SocketHandshakeService#handshake(ServerWebExchange)
 * @since 0.1.0
 */
public class WebSocketPrincipal<T> implements Principal, Serializable {
    private final T authentication;

    public WebSocketPrincipal(@NonNull final T authentication) {
        this.authentication = authentication;
    }

    /**
     * See {@link Principal#getName()}
     */
    @Override
    public String getName() {
        return WebSocketPrincipal.class.getSimpleName();
    }

    public T getAuthentication() {
        return authentication;
    }
}
