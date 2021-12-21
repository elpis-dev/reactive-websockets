package org.elpis.reactive.websockets.exception;

/**
 * Exception class used generally for validation failures.
 *
 * @author Alex Zharkov
 * @since 0.1.0
 */
public class ValidationException extends RuntimeException {

    public ValidationException(final String message) {
        super(message);
    }
}
