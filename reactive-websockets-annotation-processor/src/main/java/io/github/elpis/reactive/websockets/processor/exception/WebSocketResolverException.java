package io.github.elpis.reactive.websockets.processor.exception;

public class WebSocketResolverException extends RuntimeException {

    public WebSocketResolverException(final String message) {
        super(message);
    }

    public WebSocketResolverException(final String message, Object... args) {
        super(String.format(message, args));
    }
}
