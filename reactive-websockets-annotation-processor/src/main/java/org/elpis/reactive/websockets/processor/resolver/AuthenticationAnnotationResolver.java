package org.elpis.reactive.websockets.processor.resolver;

import com.squareup.javapoet.CodeBlock;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public final class AuthenticationAnnotationResolver extends SocketApiAnnotationResolver<AuthenticationPrincipal> {

    AuthenticationAnnotationResolver(Elements elements, Types types) {
        super(elements, types);
    }

    @Override
    public CodeBlock resolve(final VariableElement parameter) {
        final TypeMirror parameterType = parameter.asType();
        final AuthenticationPrincipal annotation = parameter.getAnnotation(this.getAnnotationType());

//        final Principal principal = context.getAuthentication();
//        final Class<?> principalType = principal.getClass();
//
//        if (Anonymous.class.isAssignableFrom(principalType)) {
//            return null;
//        } else if (WebSocketPrincipal.class.isAssignableFrom(principalType)) {
//            final WebSocketPrincipal<?> webSocketPrincipal = TypeUtils.cast(principal, WebSocketPrincipal.class);
//
//            if (!Principal.class.isAssignableFrom(parameterType) && !parameterType.isAssignableFrom(webSocketPrincipal.getAuthentication().getClass())) {
//                throw new WebSocketConfigurationException("Unable register method `%s()`. Requested @SocketAuthentication type: %s, found: %s",
//                        parameter.getDeclaringExecutable().getName(), parameterType.getName(), webSocketPrincipal.getAuthentication().getClass().getName());
//            }
//
//            return Principal.class.isAssignableFrom(parameterType)
//                    ? webSocketPrincipal
//                    : webSocketPrincipal.getAuthentication();
//        } else if (Authentication.class.isAssignableFrom(principalType)) {
//            final Authentication authentication = TypeUtils.cast(principal, Authentication.class);
//
//            return Authentication.class.isAssignableFrom(parameterType)
//                    ? authentication
//                    : this.getPrincipalDetails(authentication, parameterType);
//        } else {
//            return principal;
//        }
        return CodeBlock.builder().build();
    }

    @Override
    Class<AuthenticationPrincipal> getAnnotationType() {
        return AuthenticationPrincipal.class;
    }
}