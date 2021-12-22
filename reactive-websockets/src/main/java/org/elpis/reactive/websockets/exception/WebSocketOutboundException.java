package org.elpis.reactive.websockets.exception;

/**
 * Exception class used generally for pre-launch Spring configuration processes failures.
 *
 * @author Alex Zharkov
 * @since 0.1.0
 */
public class WebSocketOutboundException extends RuntimeException {

    public WebSocketOutboundException(final String message) {
        super(message);
    }

    public WebSocketOutboundException(final String message, Object... args) {
        super(String.format(message, args));
    }
}
