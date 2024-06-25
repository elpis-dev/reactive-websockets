package org.elpis.reactive.websockets.exception;

/**
 * Exception class used generally for pre-launch Spring configuration processes failures.
 *
 * @author Alex Zharkov
 * @since 0.1.0
 */
public class WebSocketMappingException extends RuntimeException {

    public WebSocketMappingException(final String message) {
        super(message);
    }

    public WebSocketMappingException(final String message, Object... args) {
        super(String.format(message, args));
    }
}
