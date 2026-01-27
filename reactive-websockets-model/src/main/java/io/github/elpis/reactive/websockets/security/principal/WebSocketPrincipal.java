package io.github.elpis.reactive.websockets.security.principal;

import java.io.Serializable;
import java.security.Principal;
import org.springframework.lang.NonNull;

/**
 * {@link Principal Principal}-wrapper class that puts a custom {@link Object} into security
 * context. Usually will wrap any returned from SocketHandshakeService.
 *
 * <pre class="code">
 * public Mono<?> handshake(final ServerWebExchange exchange) {
 *   return Mono.just(new MyCustomObject());
 * }
 * </pre>
 *
 * @author Phillip J. Fry
 * @see Principal
 * @since 1.0.0
 */
public record WebSocketPrincipal<T>(T authentication) implements Principal, Serializable {
  public WebSocketPrincipal(@NonNull final T authentication) {
    this.authentication = authentication;
  }

  /** See {@link Principal#getName()} */
  @Override
  public String getName() {
    return WebSocketPrincipal.class.getSimpleName();
  }
}
