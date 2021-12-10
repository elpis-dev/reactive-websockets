package org.elpis.reactive.websockets.config.annotation;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public interface SocketApiAnnotationEvaluator<A extends Annotation> {

    Object evaluate(@NonNull WebSocketSessionContext webSocketSessionContext,
                    @NonNull Parameter parameter, @NonNull String methodName,
                    @NonNull A annotation);

    @NonNull Class<A> getAnnotationType();
}
