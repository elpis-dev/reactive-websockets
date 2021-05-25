package org.elpis.reactive.websockets.config.annotations;

import lombok.NonNull;
import org.elpis.reactive.websockets.web.model.WebSocketSessionContext;

import java.lang.annotation.Annotation;

public abstract class SocketApiAnnotationEvaluator<A extends Annotation> {

    public abstract Object evaluate(@NonNull WebSocketSessionContext webSocketSessionContext,
                                    @NonNull Class<?> parameterType, @NonNull String methodName,
                                    @NonNull A annotation);

    @NonNull
    public abstract Class<A> getAnnotationType();

    public boolean supported(@NonNull final Class<? extends Annotation> annotationType) {
        return this.getAnnotationType().isAssignableFrom(annotationType);
    }
}
