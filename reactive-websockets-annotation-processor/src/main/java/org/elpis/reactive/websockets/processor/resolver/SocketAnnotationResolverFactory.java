package org.elpis.reactive.websockets.processor.resolver;

import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SocketAnnotationResolverFactory {
    private static final Map<Class<? extends Annotation>, BiFunction<Elements, Types, SocketApiAnnotationResolver<?>>> BASE_SUPPORTED_RESOLVERS = Map.of(
            RequestHeader.class, HeaderAnnotationResolver::new,
            PathVariable.class, PathVariableAnnotationResolver::new,
            AuthenticationPrincipal.class, AuthenticationAnnotationResolver::new,
            RequestParam.class, QueryParamAnnotationResolver::new,
            RequestBody.class, SocketMessageBodyAnnotationResolver::new
    );

    private static final Map<Class<? extends Annotation>,
            BiFunction<Elements, Types, SocketApiAnnotationResolver<?>>> SUPPORTED_RESOLVERS = new ConcurrentHashMap<>();

    private SocketAnnotationResolverFactory() {
    }

    public static Optional<BiFunction<Elements, Types, SocketApiAnnotationResolver<?>>> findResolver(final VariableElement element) {
        final List<BiFunction<Elements, Types, SocketApiAnnotationResolver<?>>> annotationsFiltered = Stream.concat(BASE_SUPPORTED_RESOLVERS.entrySet().stream(),
                        SUPPORTED_RESOLVERS.entrySet().stream())
                .filter(entry -> element.getAnnotation(entry.getKey()) != null)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (annotationsFiltered.size() > 1) {
            final String failedAnnotations = annotationsFiltered.stream()
                    .map(annotation -> "@" + annotation.getClass().getName())
                    .collect(Collectors.joining(","));

            throw new WebSocketConfigurationException("Ambiguous WebSocket annotations found %s. " +
                    "Only one declared annotation is legal", failedAnnotations);
        }

        return annotationsFiltered.stream()
                .findFirst();
    }

    public static <A extends Annotation> void registerResolver(final Class<A> annotationType,
                                                               final Class<SocketApiAnnotationResolver<A>> resolverClass) {


        if (BASE_SUPPORTED_RESOLVERS.containsKey(annotationType)) {
            throw new WebSocketConfigurationException("Base resolvers cannot be overridden: @%s is already registered",
                    annotationType.getSimpleName());
        }

        SUPPORTED_RESOLVERS.put(annotationType, (elements, types) -> {
            try {
                final SocketApiAnnotationResolver<A> resolver = resolverClass.getDeclaredConstructor()
                        .newInstance();
                resolver.setAnnotationType(annotationType);
                return resolver;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new WebSocketConfigurationException(e.getMessage());
            }
        });
    }
}
