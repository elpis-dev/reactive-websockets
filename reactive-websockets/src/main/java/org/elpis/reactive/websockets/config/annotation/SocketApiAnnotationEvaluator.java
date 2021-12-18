package org.elpis.reactive.websockets.config.annotation;

import lombok.NonNull;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Contract to process system or custom parameter annotation that would we used within {@link org.elpis.reactive.websockets.web.annotation.controller.Inbound @Inbound} or {@link org.elpis.reactive.websockets.web.annotation.controller.Outbound @Outbound} annotated methods.
 *
 * @author Alex Zharkov
 * @see org.elpis.reactive.websockets.web.annotation.controller.Inbound
 * @see org.elpis.reactive.websockets.web.annotation.controller.Outbound
 * @since 0.1.0
 */
public interface SocketApiAnnotationEvaluator<A extends Annotation> {

    /**
     * Custom evaluation of annotated parameter.
     *
     * @param webSocketSessionContext session context object to process
     * @param parameter method parameter marked with annotation of {@link A} type
     * @param methodName the name of method with annotated parameter
     * @param annotation the annotation instance
     * @return any object that should be passed to annotated parameter
     * @since 0.1.0
     */
    Object evaluate(@NonNull WebSocketSessionContext webSocketSessionContext, @NonNull Parameter parameter,
                    @NonNull String methodName, @NonNull A annotation);

    /**
     * @return {@link Class Class<A>}
     * @since 0.1.0
     */
    @NonNull Class<A> getAnnotationType();
}
