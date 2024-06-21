package org.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class SocketMessageBodyAnnotationResolver extends SocketApiAnnotationResolver<RequestBody> {
    private static final String GET_BODY = "final Flux<WebSocketMessage> $L = messages;\n";

    SocketMessageBodyAnnotationResolver(Elements elements, Types types) {
        super(elements, types);
    }

    @Override
    public CodeBlock resolve(final VariableElement parameter) {
        final TypeMirror parameterType = parameter.asType();

        final Element publisherType = this.getElements().getTypeElement(Flux.class.getCanonicalName());
        if (!this.getTypes().isSameType(this.getTypes().erasure(parameterType),
                this.getTypes().erasure(publisherType.asType()))) {

            throw new WebSocketConfigurationException("@RequestBody should be used with Flux type. Found: %s", parameterType);
        }

        return CodeBlock.of(GET_BODY, parameter.getSimpleName());
    }

    @Override
    Class<RequestBody> getAnnotationType() {
        return RequestBody.class;
    }
}
