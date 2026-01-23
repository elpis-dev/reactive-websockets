package io.github.elpis.reactive.websockets.event.matcher.impl;

import io.github.elpis.reactive.websockets.Constants;
import io.github.elpis.reactive.websockets.config.SessionCloseInfo;
import io.github.elpis.reactive.websockets.event.annotation.EventSelector;
import io.github.elpis.reactive.websockets.event.matcher.ReactiveWebSocketEventSelectorMatcher;
import io.github.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;

/**
 * Implementation of {@link ReactiveWebSocketEventSelectorMatcher} for {@link
 * ClientSessionClosedEvent} events.
 *
 * @author Phillip J. Fry
 * @see ReactiveWebSocketEventSelectorMatcher
 * @see ClientSessionClosedEvent
 * @since 1.0.0
 */
public class ClosedSessionReactiveWebSocketEventSelectorMatcher
    implements ReactiveWebSocketEventSelectorMatcher<ClientSessionClosedEvent> {

  /**
   * Matches {@link ClientSessionClosedEvent} against {@link EventSelector} using SpEL expression
   * defined in the annotation.
   *
   * @param event ClientSessionClosedEvent event
   * @param annotation EventSelector annotation from annotated method
   * @return boolean signaling about match success/failure
   * @since 1.0.0
   */
  @Override
  public boolean matches(final ClientSessionClosedEvent event, final EventSelector annotation) {
    final SessionCloseInfo sessionCloseInfo = event.payload();
    return Boolean.TRUE.equals(
        Constants.parseExpression(
            annotation.value(),
            Boolean.class,
            (builder ->
                builder
                    .withAssignmentDisabled()
                    .withRootObject(
                        SessionCloseInfo.builder().session(sessionCloseInfo.getSession()).build())),
            (__ -> {})));
  }
}
