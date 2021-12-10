package org.elpis.reactive.websockets.event;

import org.elpis.reactive.websockets.event.model.WebSocketEvent;

import java.lang.annotation.Annotation;

public interface EventSelectorProcessor<E extends WebSocketEvent<?>, A extends Annotation, R> {
    R select(E event, A annotation);
}
