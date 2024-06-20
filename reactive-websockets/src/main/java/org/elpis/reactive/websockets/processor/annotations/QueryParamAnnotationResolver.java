package org.elpis.reactive.websockets.processor.annotations;

import com.squareup.javapoet.CodeBlock;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

public final class QueryParamAnnotationResolver extends SocketApiAnnotationResolver<RequestParam> {
    private static final String CODE_FOR_GET_LIST_QUERY_REQUIRED = "final $T $L = context.getQueryParams($S, $S, $T.class);\n"
            + "if ($L.isEmpty())\n throw new org.elpis.reactive.websockets.exception.WebSocketProcessingException($S);\n";

    private static final String CODE_FOR_GET_LIST_QUERY = "final $T $L = context.getQueryParams($S, $S, $T.class);\n";

    private static final String CODE_FOR_GET_SINGLE_QUERY_REQUIRED = "final $T $L = context.getQueryParam($S, $S, $T.class)\n" +
            ".orElseThrow(() -> new org.elpis.reactive.websockets.exception.WebSocketProcessingException($S));\n";

    private static final String CODE_FOR_GET_SINGLE_QUERY = "final $T $L = context.getQueryParam($S, $S, $T.class)\n" +
            ".orElseGet(() -> org.elpis.reactive.websockets.util.TypeUtils.getDefaultValueForType($T.class));\n";

    QueryParamAnnotationResolver(Elements elements, Types types) {
        super(elements, types);
    }

    @Override
    public CodeBlock resolve(final VariableElement parameter) {
        final TypeMirror parameterType = parameter.asType();
        final RequestParam annotation = parameter.getAnnotation(this.getAnnotationType());

        final Element listType = this.getElements().getTypeElement(List.class.getCanonicalName());

        final String defaultValue = Optional.of(annotation.defaultValue())
                .filter(value -> !value.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(value))
                .orElse(null);

        if (this.getTypes().isAssignable(this.getTypes().erasure(parameterType), listType.asType())) {
            if (annotation.value().isEmpty()) {
                throw new WebSocketConfigurationException("Value cannot be empty at @SocketQueryParam %s %s",
                        parameterType, parameter.getSimpleName().toString());
            }

            if (parameterType instanceof DeclaredType) {
                final DeclaredType declaredReturnType = (DeclaredType) parameterType;
                final TypeMirror listDeclaredType = declaredReturnType.getTypeArguments().get(0);

                if (annotation.required()) {
                    return CodeBlock.of(CODE_FOR_GET_LIST_QUERY_REQUIRED,
                            parameterType,
                            parameter.getSimpleName().toString(),
                            annotation.value(),
                            defaultValue,
                            listDeclaredType,
                            parameter.getSimpleName().toString(),
                            "Header is marked as required but was not present on request. Default value was not set.");
                } else {
                    return CodeBlock.of(CODE_FOR_GET_LIST_QUERY,
                            parameterType,
                            parameter.getSimpleName().toString(),
                            annotation.value(),
                            defaultValue,
                            listDeclaredType);
                }
            } else {
                throw new WebSocketConfigurationException("Cannot process @SocketQueryParam parameter: bad return type: %s",
                        parameterType);
            }
        } else {
            if (annotation.value().isEmpty()) {
                throw new WebSocketConfigurationException("Value cannot be empty at @SocketQueryParam %s %s",
                        parameterType, parameter.getSimpleName().toString());
            }

            if (annotation.required()) {
                return CodeBlock.of(CODE_FOR_GET_SINGLE_QUERY_REQUIRED,
                        parameterType,
                        parameter.getSimpleName().toString(),
                        annotation.value(),
                        defaultValue,
                        parameterType,
                        String.format("Header '%s' is marked as required but was not present on request. " +
                                "Default value was not set.", parameter.getSimpleName().toString()));
            } else {
                return CodeBlock.of(CODE_FOR_GET_SINGLE_QUERY,
                        parameterType,
                        parameter.getSimpleName().toString(),
                        annotation.value(),
                        defaultValue,
                        parameterType,
                        parameterType);
            }
        }
    }

    @Override
    Class<RequestParam> getAnnotationType() {
        return RequestParam.class;
    }
}