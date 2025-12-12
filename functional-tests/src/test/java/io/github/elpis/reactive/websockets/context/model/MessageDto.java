package io.github.elpis.reactive.websockets.context.model;

/**
 * @since 1.0.0
 * @param timestamp the message timestamp
 * @param text the message text
 *     <p>Simple POJO for testing JSON deserialization.
 */
public record MessageDto(String text, Long timestamp) {}
