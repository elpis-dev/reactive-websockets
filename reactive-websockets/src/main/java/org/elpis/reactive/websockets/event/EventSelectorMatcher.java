package org.elpis.reactive.websockets.event;

import org.elpis.reactive.websockets.event.model.WebSocketEvent;

import java.lang.annotation.Annotation;

public interface EventSelectorMatcher<E extends WebSocketEvent<?>, A extends Annotation> extends EventSelectorProcessor<E, A, Boolean> {
}
