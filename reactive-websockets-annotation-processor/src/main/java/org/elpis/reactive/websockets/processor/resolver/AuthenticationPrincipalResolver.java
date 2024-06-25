package org.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.elpis.reactive.websockets.security.principal.WebSocketPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public final class AuthenticationPrincipalResolver extends SocketApiAnnotationResolver<AuthenticationPrincipal> {
    private static final String CODE_FOR_PRINCIPAL = "final $T $L = context.getPrincipal($S, $L, $T.class);\n";

    AuthenticationPrincipalResolver(Elements elements, Types types) {
        super(elements, types);
    }

    @Override
    public CodeBlock resolve(final VariableElement parameter) {
        final TypeMirror parameterType = parameter.asType();
        final AuthenticationPrincipal annotation = parameter.getAnnotation(this.getAnnotationType());

        final Element principalType = this.getElements().getTypeElement(WebSocketPrincipal.class.getCanonicalName());
        final TypeName parameterTypeValue = this.getTypes().isSameType(this.getTypes().erasure(parameterType),
                this.getTypes().erasure(principalType.asType()))
                ? ClassName.get(WebSocketPrincipal.class)
                : TypeName.get(parameterType);

        return CodeBlock.of(CODE_FOR_PRINCIPAL,
                parameterTypeValue,
                parameter.getSimpleName(),
                annotation.expression(),
                annotation.errorOnInvalidType(),
                parameterTypeValue);
    }

    @Override
    Class<AuthenticationPrincipal> getAnnotationType() {
        return AuthenticationPrincipal.class;
    }
}