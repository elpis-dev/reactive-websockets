package org.elpis.reactive.websockets.exception;

/**
 * Exception class used generally for pre-launch Spring configuration processes failures.
 *
 * @author Alex Zharkov
 * @since 0.1.0
 */
public class WebSocketInboundException extends RuntimeException {

    public WebSocketInboundException(final String message) {
        super(message);
    }

    public WebSocketInboundException(final String message, Object... args) {
        super(String.format(message, args));
    }
}
