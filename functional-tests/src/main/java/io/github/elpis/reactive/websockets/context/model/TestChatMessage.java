package io.github.elpis.reactive.websockets.context.model;

import java.util.List;

/**
 * POJO with collections for testing JSON deserialization.
 *
 * @param chatId the chat ID
 * @param recipients the list of recipients
 * @param text the chat message text
 * @since 1.0.0
 */
public record TestChatMessage(String chatId, List<String> recipients, String text) {}
