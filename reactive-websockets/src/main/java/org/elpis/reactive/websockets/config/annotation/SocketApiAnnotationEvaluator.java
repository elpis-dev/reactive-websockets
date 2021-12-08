package org.elpis.reactive.websockets.config.annotation;

import lombok.NonNull;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;

import java.lang.annotation.Annotation;

public interface SocketApiAnnotationEvaluator<A extends Annotation> {

    Object evaluate(@NonNull WebSocketSessionContext webSocketSessionContext,
                    @NonNull Class<?> parameterType, @NonNull String methodName,
                    @NonNull A annotation);

    @NonNull Class<A> getAnnotationType();

    default boolean supported(@NonNull final Class<? extends Annotation> annotationType) {
        return this.getAnnotationType().isAssignableFrom(annotationType);
    }
}
