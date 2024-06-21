package org.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;
import org.springframework.web.bind.annotation.PathVariable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public final class PathVariableAnnotationResolver extends SocketApiAnnotationResolver<PathVariable> {
    private static final String CODE_FOR_GET_PATH_REQUIRED = "final $T $L = context.getPathVariable($S, $T.class)\n" +
            ".orElseThrow(() -> new org.elpis.reactive.websockets.exception.WebSocketProcessingException($S));\n";

    private static final String CODE_FOR_GET_SINGLE_PATH = "final $T $L = context.getPathVariable($S, $T.class)\n" +
            ".orElseGet(() -> org.elpis.reactive.websockets.util.TypeUtils.getDefaultValueForType($T.class));\n";

    PathVariableAnnotationResolver(Elements elements, Types types) {
        super(elements, types);
    }


    @Override
    public CodeBlock resolve(final VariableElement parameter) {
        final TypeMirror parameterType = parameter.asType();
        final PathVariable annotation = parameter.getAnnotation(this.getAnnotationType());

        if (annotation.required()) {
            return CodeBlock.of(CODE_FOR_GET_PATH_REQUIRED,
                    parameterType,
                    parameter.getSimpleName().toString(),
                    annotation.value(),
                    parameterType,
                    "Header is marked as required but was not present on request. Default value was not set.");
        } else {
            return CodeBlock.of(CODE_FOR_GET_SINGLE_PATH,
                    parameterType,
                    parameter.getSimpleName().toString(),
                    annotation.value(),
                    parameterType,
                    parameterType);
        }
    }

    @Override
    Class<PathVariable> getAnnotationType() {
        return PathVariable.class;
    }
}