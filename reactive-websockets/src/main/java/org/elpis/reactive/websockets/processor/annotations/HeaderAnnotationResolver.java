package org.elpis.reactive.websockets.processor.annotations;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ValueConstants;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

public final class HeaderAnnotationResolver extends SocketApiAnnotationResolver<RequestHeader> {
    private static final String CODE_FOR_GET_HEADERS = "final $T $L = context.getHeaders();\n";

    private static final String CODE_FOR_GET_LIST_HEADER_REQUIRED = "final $T $L = context.getHeaders($S, $S, $T.class);\n"
            + "if ($L.isEmpty())\n throw new org.elpis.reactive.websockets.exception.WebSocketProcessingException($S);\n";

    private static final String CODE_FOR_GET_LIST_HEADER = "final $T $L = context.getHeaders($S, $S, $T.class);\n";

    private static final String CODE_FOR_GET_SINGLE_HEADER_REQUIRED = "final $T $L = context.getHeader($S, $S, $T.class)\n" +
            ".orElseThrow(() -> new org.elpis.reactive.websockets.exception.WebSocketProcessingException($S));\n";

    private static final String CODE_FOR_GET_SINGLE_HEADER = "final $T $L = context.getHeader($S, $S, $T.class)\n" +
            ".orElseGet(() -> org.elpis.reactive.websockets.util.TypeUtils.getDefaultValueForType($T.class));\n";

    HeaderAnnotationResolver(Elements elements, Types types) {
        super(elements, types);
    }

    @Override
    public CodeBlock resolve(final VariableElement parameter) {
        final TypeMirror parameterType = parameter.asType();
        final RequestHeader annotation = parameter.getAnnotation(this.getAnnotationType());

        final Element httpHeadersType = this.getElements().getTypeElement(HttpHeaders.class.getCanonicalName());
        final Element multiValueMapType = this.getElements().getTypeElement(MultiValueMap.class.getCanonicalName());
        final Element listType = this.getElements().getTypeElement(List.class.getCanonicalName());

        final String defaultValue = Optional.of(annotation.defaultValue())
                .filter(value -> !value.isEmpty() && !ValueConstants.DEFAULT_NONE.equals(value))
                .orElse(null);

        if (this.getTypes().isAssignable(parameterType, httpHeadersType.asType())) {
            return CodeBlock.of(CODE_FOR_GET_HEADERS, HttpHeaders.class, parameter.getSimpleName().toString());
        } else if (this.getTypes().isAssignable(this.getTypes().erasure(parameterType), multiValueMapType.asType())) {
            if (!this.isMultimapParamValid(parameterType)) {
                throw new WebSocketConfigurationException("Request header `@SocketHeader %s %s` should accept " +
                        "`org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>`, but got `%s`", parameterType, parameter.getSimpleName(), parameterType);
            }

            final TypeName multiValueMapTypeName = ParameterizedTypeName.get(ClassName.get(MultiValueMap.class),
                    TypeName.get(String.class), TypeName.get(String.class));

            return CodeBlock.of(CODE_FOR_GET_HEADERS, multiValueMapTypeName, parameter.getSimpleName().toString());
        } else if (this.getTypes().isAssignable(this.getTypes().erasure(parameterType), listType.asType())) {
            if (annotation.value().isEmpty()) {
                throw new WebSocketConfigurationException("Value cannot be empty at @SocketHeader %s %s",
                        parameterType, parameter.getSimpleName().toString());
            }

            if (parameterType instanceof DeclaredType) {
                final DeclaredType declaredReturnType = (DeclaredType) parameterType;
                final TypeMirror listDeclaredType = declaredReturnType.getTypeArguments().get(0);

                if (annotation.required()) {
                    return CodeBlock.of(CODE_FOR_GET_LIST_HEADER_REQUIRED,
                            parameterType,
                            parameter.getSimpleName().toString(),
                            annotation.value(),
                            defaultValue,
                            listDeclaredType,
                            parameter.getSimpleName().toString(),
                            "Header is marked as required but was not present on request. Default value was not set.");
                } else {
                    return CodeBlock.of(CODE_FOR_GET_LIST_HEADER,
                            parameterType,
                            parameter.getSimpleName().toString(),
                            annotation.value(),
                            defaultValue,
                            listDeclaredType);
                }
            } else {
                throw new WebSocketConfigurationException("Cannot process @SocketHeader parameter: bad return type: %s",
                        parameterType);
            }
        } else {
            if (annotation.value().isEmpty()) {
                throw new WebSocketConfigurationException("Value cannot be empty at @SocketHeader %s %s",
                        parameterType, parameter.getSimpleName().toString());
            }

            if (annotation.required()) {
                return CodeBlock.of(CODE_FOR_GET_SINGLE_HEADER_REQUIRED,
                        parameterType,
                        parameter.getSimpleName().toString(),
                        annotation.value(),
                        defaultValue,
                        parameterType,
                        String.format("Header '%s' is marked as required but was not present on request. " +
                                "Default value was not set.", parameter.getSimpleName().toString()));
            } else {
                return CodeBlock.of(CODE_FOR_GET_SINGLE_HEADER,
                        parameterType,
                        parameter.getSimpleName().toString(),
                        annotation.value(),
                        defaultValue,
                        parameterType,
                        parameterType);
            }
        }
    }

    private boolean isMultimapParamValid(final TypeMirror type) {
        if (type instanceof DeclaredType) {
            final DeclaredType declaredReturnType = (DeclaredType) type;
            final TypeMirror keyType = declaredReturnType.getTypeArguments().get(0);
            final TypeMirror valueType = declaredReturnType.getTypeArguments().get(1);

            final TypeMirror stringType = this.getElements().getTypeElement(String.class.getCanonicalName()).asType();

            return this.getTypes().isSameType(stringType, keyType) && this.getTypes().isSameType(valueType, stringType);
        } else {
            throw new WebSocketConfigurationException("Cannot process @SocketHeader parameter: bad return type: %s", type);
        }
    }

    @Override
    Class<RequestHeader> getAnnotationType() {
        return RequestHeader.class;
    }
}