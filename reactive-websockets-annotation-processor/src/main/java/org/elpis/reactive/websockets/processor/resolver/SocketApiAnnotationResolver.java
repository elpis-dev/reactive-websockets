package org.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;

public abstract class SocketApiAnnotationResolver<A extends Annotation> {
    private Class<A> annotationType;

    private final Elements elements;
    private final Types types;

    SocketApiAnnotationResolver(final Elements elements, final Types types) {
        this.elements = elements;
        this.types = types;
    }

    public abstract CodeBlock resolve(VariableElement parameter);

    Class<A> getAnnotationType() {
        return annotationType;
    }

    void setAnnotationType(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    public Elements getElements() {
        return elements;
    }

    public Types getTypes() {
        return types;
    }
}