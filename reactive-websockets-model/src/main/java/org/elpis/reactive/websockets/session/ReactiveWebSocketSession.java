package org.elpis.reactive.websockets.session;

import org.springframework.web.reactive.socket.CloseStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

public class ReactiveWebSocketSession {
    private final String sessionId;

    private final BooleanSupplier isOpen;
    private final BiConsumer<String, CloseStatus> onClose;

    private final Instant timestamp = Instant.now();

    private ReactiveWebSocketSession(final String sessionId,
                                     final BooleanSupplier isOpen,
                                     final BiConsumer<String, CloseStatus> onClose) {

        this.sessionId = sessionId;
        this.isOpen = isOpen;
        this.onClose = onClose;
    }

    public boolean isOpen() {
        return this.isOpen.getAsBoolean();
    }

    public void close() {
        this.close(CloseStatus.NORMAL);
    }

    public void close(final CloseStatus closeStatus) {
        this.onClose.accept(this.sessionId, closeStatus);
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

    public static class Builder {
        private BooleanSupplier isOpen = () -> true;
        private BiConsumer<String, CloseStatus> onClose;
        private String sessionId;

        public Builder isOpen(BooleanSupplier isOpen) {
            this.isOpen = Optional.ofNullable(isOpen)
                    .orElseGet(() -> () -> true);

            return this;
        }

        public Builder sessionId(final String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder onClose(final BiConsumer<String, CloseStatus> onClose) {
            this.onClose = onClose;
            return this;
        }

        public ReactiveWebSocketSession build() {
            return new ReactiveWebSocketSession(this.sessionId, this.isOpen, this.onClose);
        }
    }
}
