package org.elpis.reactive.websockets.processor.exception;

public class WebSocketProcessorException extends RuntimeException {

    public WebSocketProcessorException(final String message) {
        super(message);
    }

    public WebSocketProcessorException(final String message, Object... args) {
        super(String.format(message, args));
    }
}
