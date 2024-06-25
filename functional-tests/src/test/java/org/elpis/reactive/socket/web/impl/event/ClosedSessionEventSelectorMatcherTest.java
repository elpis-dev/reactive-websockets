package org.elpis.reactive.socket.web.impl.event;

import org.elpis.reactive.websockets.config.model.ClientSessionCloseInfo;
import org.elpis.reactive.websockets.config.registry.WebSocketSessionInfo;
import org.elpis.reactive.websockets.event.annotation.EventSelector;
import org.elpis.reactive.websockets.event.impl.ClosedSessionEventSelectorMatcher;
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

public class ClosedSessionEventSelectorMatcherTest {
    private static final String ID = UUID.randomUUID().toString();
    private static final String HOST = "localhost";
    private static final int PORT = 80;
    private static final String PATH = "/test/1";

    @Mock
    private EventSelector eventSelector;

    private final ClosedSessionEventSelectorMatcher eventSelectorMatcher = new ClosedSessionEventSelectorMatcher();

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSelectSingleFieldMatch() {
        //given
        final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                .id(ID)
                .host(HOST)
                .port(PORT)
                .path(PATH)
                .build();

        final ClientSessionCloseInfo clientSessionCloseInfo = ClientSessionCloseInfo.builder()
                .sessionInfo(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = ClientSessionClosedEvent.builder()
                .clientSessionCloseInfo(clientSessionCloseInfo)
                .build();

        //mock
        when(eventSelector.value()).thenReturn("sessionInfo.path matches '\\/test\\/\\d+'");

        //test
        final boolean result = eventSelectorMatcher.select(event, eventSelector);

        //assert
        assertTrue(result);

        //verify
        verify(eventSelector, times(1)).value();
    }

    @Test
    public void testSelectMultipleFieldsMatch() {
        //given
        final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                .id(ID)
                .host(HOST)
                .port(PORT)
                .path(PATH)
                .build();

        final ClientSessionCloseInfo clientSessionCloseInfo = ClientSessionCloseInfo.builder()
                .sessionInfo(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = ClientSessionClosedEvent.builder()
                .clientSessionCloseInfo(clientSessionCloseInfo)
                .build();

        //mock
        when(eventSelector.value()).thenReturn("sessionInfo.path matches '\\/test\\/\\d+' and sessionInfo.port != 443");

        //test
        final boolean result = eventSelectorMatcher.select(event, eventSelector);

        //assert
        assertTrue(result);

        //verify
        verify(eventSelector, times(1)).value();
    }

    @Test
    public void testSelectNoMatch() {
        //given
        final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                .id(ID)
                .host(HOST)
                .port(PORT)
                .path(PATH)
                .build();

        final ClientSessionCloseInfo clientSessionCloseInfo = ClientSessionCloseInfo.builder()
                .sessionInfo(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = ClientSessionClosedEvent.builder()
                .clientSessionCloseInfo(clientSessionCloseInfo)
                .build();

        //mock
        when(eventSelector.value()).thenReturn("sessionInfo.path matches '\\/api\\/\\d+'");

        //test
        final boolean result = eventSelectorMatcher.select(event, eventSelector);

        //assert
        assertFalse(result);

        //verify
        verify(eventSelector, times(1)).value();
    }

    @Test
    public void testSelectWrongSpelFormat() {
        //given
        final WebSocketSessionInfo webSocketSessionInfo = WebSocketSessionInfo.builder()
                .id(ID)
                .host(HOST)
                .port(PORT)
                .path(PATH)
                .build();

        final ClientSessionCloseInfo clientSessionCloseInfo = ClientSessionCloseInfo.builder()
                .sessionInfo(webSocketSessionInfo)
                .build();

        final ClientSessionClosedEvent event = ClientSessionClosedEvent.builder()
                .clientSessionCloseInfo(clientSessionCloseInfo)
                .build();

        //mock
        when(eventSelector.value()).thenReturn("session.path matches '\\/test\\/\\d+'");

        //assert
        Assertions.assertThrows(SpelEvaluationException.class, () -> eventSelectorMatcher.select(event, eventSelector));
    }

}
