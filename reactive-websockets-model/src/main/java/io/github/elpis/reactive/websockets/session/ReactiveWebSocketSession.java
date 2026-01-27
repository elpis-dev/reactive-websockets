package io.github.elpis.reactive.websockets.session;

import java.time.Instant;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import org.springframework.web.reactive.socket.CloseStatus;
import reactor.core.publisher.Mono;

/**
 * Represents a reactive WebSocket session with capabilities to check its status and close it.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public class ReactiveWebSocketSession {
  private final String sessionId;
  private final BooleanSupplier isOpen;
  private final BiFunction<String, CloseStatus, Mono<Void>> onClose;

  private final Instant timestamp = Instant.now();

  private ReactiveWebSocketSession(
      final String sessionId,
      final BooleanSupplier isOpen,
      final BiFunction<String, CloseStatus, Mono<Void>> onClose) {

    this.sessionId = sessionId;
    this.isOpen = isOpen;
    this.onClose = onClose;
  }

  public boolean isOpen() {
    return this.isOpen.getAsBoolean();
  }

  public Mono<Void> close() {
    return this.close(CloseStatus.NORMAL);
  }

  public Mono<Void> close(final CloseStatus closeStatus) {
    return this.onClose.apply(this.sessionId, closeStatus);
  }

  public String getSessionId() {
    return sessionId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for constructing instances of {@link ReactiveWebSocketSession}.
   *
   * @author Phillip J. Fry
   * @since 1.0.0
   */
  public static class Builder {
    private BooleanSupplier isOpen = () -> true;
    private BiFunction<String, CloseStatus, Mono<Void>> onClose;
    private String sessionId;

    public Builder isOpen(BooleanSupplier isOpen) {
      this.isOpen = Optional.ofNullable(isOpen).orElseGet(() -> () -> true);

      return this;
    }

    public Builder sessionId(final String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder onClose(final BiFunction<String, CloseStatus, Mono<Void>> onClose) {
      this.onClose = onClose;
      return this;
    }

    public ReactiveWebSocketSession build() {
      return new ReactiveWebSocketSession(this.sessionId, this.isOpen, this.onClose);
    }
  }
}
