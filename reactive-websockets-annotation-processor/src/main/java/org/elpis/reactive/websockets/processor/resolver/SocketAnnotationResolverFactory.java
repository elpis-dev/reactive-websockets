package org.elpis.reactive.websockets.processor.resolver;

import org.elpis.reactive.websockets.processor.exception.WebSocketProcessorException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

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
            RequestHeader.class, RequestHeaderResolver::new,
            PathVariable.class, PathVariableResolver::new,
            AuthenticationPrincipal.class, AuthenticationPrincipalResolver::new,
            RequestParam.class, RequestParamResolver::new,
            RequestBody.class, RequestBodyResolver::new,
            SessionAttribute.class, SessionResolver::new
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
                .toList();

        if (annotationsFiltered.size() > 1) {
            final String failedAnnotations = annotationsFiltered.stream()
                    .map(annotation -> "@" + annotation.getClass().getName())
                    .collect(Collectors.joining(","));

            throw new WebSocketProcessorException("Ambiguous WebSocket annotations found %s. " +
                    "Only one declared annotation is legal", failedAnnotations);
        }

        return annotationsFiltered.stream()
                .findFirst();
    }
}
