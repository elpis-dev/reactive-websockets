package org.elpis.reactive.websockets.config.annotations;

import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;

public abstract class SocketApiAnnotationEvaluator<A extends Annotation, T> {

    public abstract Object evaluate(final T data, @NonNull final Class<?> parameterType, @NonNull final String methodName, @NonNull final A annotation);

    @NonNull
    public abstract Class<A> getAnnotationType();

    public boolean isAssignableFrom(@NonNull final Class<? extends Annotation> annotationType) {
        return this.getAnnotationType().isAssignableFrom(annotationType);
    }
}
