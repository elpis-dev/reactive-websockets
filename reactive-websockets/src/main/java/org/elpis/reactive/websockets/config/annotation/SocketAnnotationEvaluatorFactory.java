package org.elpis.reactive.websockets.config.annotation;

import lombok.NonNull;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a registry of all the implementations of {@link SocketApiAnnotationEvaluator}. Registered as Spring Bean on application startup.
 * <p>Supports custom {@link SocketApiAnnotationEvaluator} implementations.
 * <p><strong>NOTE: </strong>{@link SocketApiAnnotationEvaluator} implementations with duplicate annotations are not permitted - only one implementation per one annotation.
 *
 * @author Alex Zharkov
 * @see SocketApiAnnotationEvaluator
 * @since 0.1.0
 */
@Component
@ComponentScan(basePackageClasses = SocketApiAnnotationEvaluator.class)
public final class SocketAnnotationEvaluatorFactory {
    private final Map<Class<? extends Annotation>, SocketApiAnnotationEvaluator<?>> annotationEvaluators = new HashMap<>();

    public SocketAnnotationEvaluatorFactory(@NonNull final List<SocketApiAnnotationEvaluator<?>> annotationEvaluators) {
        annotationEvaluators.forEach(socketApiAnnotationEvaluator -> {
            final Class<? extends Annotation> annotationType = socketApiAnnotationEvaluator.getAnnotationType();
            if (this.annotationEvaluators.containsKey(annotationType)) {
                throw new WebSocketConfigurationException("Cannot register a `%s<%s>`, since there's `%s` " +
                        "already implemented with same annotation type.", socketApiAnnotationEvaluator.getClass().getName(),
                        annotationType.getName(), this.annotationEvaluators.get(annotationType).getClass().getName());
            }

            this.annotationEvaluators.put(annotationType, socketApiAnnotationEvaluator);
        });
    }

    @SuppressWarnings("rawtypes")
    public Optional<SocketApiAnnotationEvaluator> getEvaluator(@NonNull final Annotation[] annotations) {
        final List<Annotation> annotationsFiltered = Stream.of(annotations)
                .filter(annotation -> this.annotationEvaluators.containsKey(annotation.annotationType()))
                .collect(Collectors.toList());

        if (annotationsFiltered.size() > 1) {
            final String failedAnnotations = annotationsFiltered.stream()
                    .map(annotation -> "@" + annotation.getClass().getName())
                    .collect(Collectors.joining(","));

            throw new WebSocketConfigurationException("Ambiguous WebSocket annotations found %s. Only one declared annotation is legal", failedAnnotations);
        }

        return annotationsFiltered.stream()
                .findFirst()
                .map(annotation -> annotationEvaluators.get(annotation.annotationType()));
    }
}
