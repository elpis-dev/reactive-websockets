package org.elpis.reactive.websockets.impl.event;

import org.elpis.reactive.websockets.config.SessionCloseInfo;
import org.elpis.reactive.websockets.session.ReactiveWebSocketSession;
import org.elpis.reactive.websockets.event.annotation.EventSelector;
import org.elpis.reactive.websockets.event.matcher.impl.ClosedSessionEventSelectorMatcher;
import org.elpis.reactive.websockets.event.model.impl.ClientSessionClosedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.expression.spel.SpelEvaluationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ClosedSessionEventSelectorMatcherTest {
    private static final String ID = UUID.randomUUID().toString();

    @Mock
    private EventSelector eventSelector;

    private final ClosedSessionEventSelectorMatcher eventSelectorMatcher = new ClosedSessionEventSelectorMatcher();

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectSingleFieldMatch() {
        //given
        final ReactiveWebSocketSession webSocketSessionInfo = ReactiveWebSocketSession.builder()
                .sessionId(ID)
                .build();

        final SessionCloseInfo sessionCloseInfo = SessionCloseInfo.builder()
                .session(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = new ClientSessionClosedEvent(sessionCloseInfo);

        //mock
        when(eventSelector.value()).thenReturn("session.sessionId matches '" + ID + "'");

        //test
        final boolean result = eventSelectorMatcher.process(event, eventSelector);

        //assert
        assertTrue(result);

        //verify
        verify(eventSelector, times(1)).value();
    }

    @Test
    void testSelectNoMatch() {
        //given
        final ReactiveWebSocketSession webSocketSessionInfo = ReactiveWebSocketSession.builder()
                .sessionId(ID)
                .build();

        final SessionCloseInfo sessionCloseInfo = SessionCloseInfo.builder()
                .session(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = new ClientSessionClosedEvent(sessionCloseInfo);

        //mock
        when(eventSelector.value()).thenReturn("session.sessionId matches '\\/api\\/\\d+'");

        //test
        final boolean result = eventSelectorMatcher.process(event, eventSelector);

        //assert
        assertFalse(result);

        //verify
        verify(eventSelector, times(1)).value();
    }

    @Test
    void testSelectWrongSpelFormat() {
        //given
        final ReactiveWebSocketSession webSocketSessionInfo = ReactiveWebSocketSession.builder()
                .sessionId(ID)
                .build();

        final SessionCloseInfo sessionCloseInfo = SessionCloseInfo.builder()
                .session(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = new ClientSessionClosedEvent(sessionCloseInfo);

        //mock
        when(eventSelector.value()).thenReturn("session.path matches '\\/test\\/\\d+'");

        //assert
        Assertions.assertThrows(SpelEvaluationException.class, () -> eventSelectorMatcher.process(event, eventSelector));
    }

}
