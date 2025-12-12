package io.github.elpis.reactive.websockets.context.model;

/**
 * Nested POJO for testing JSON deserialization.
 *
 * @param userId the user ID
 * @param message the nested message
 * @since 1.0.0
 */
public record TestUserMessage(String userId, MessageDto message) {}
