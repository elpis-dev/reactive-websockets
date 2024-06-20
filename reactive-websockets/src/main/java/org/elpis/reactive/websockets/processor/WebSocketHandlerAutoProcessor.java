package org.elpis.reactive.websockets.processor;

import com.squareup.javapoet.*;
import org.elpis.reactive.websockets.config.handler.BroadcastWebSocketResourceHandler;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.config.registry.WebSessionRegistry;
import org.elpis.reactive.websockets.exception.WebSocketConfigurationException;
import org.elpis.reactive.websockets.mapper.JsonMapper;
import org.elpis.reactive.websockets.processor.annotations.SocketAnnotationResolverFactory;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.controller.PingPong;
import org.elpis.reactive.websockets.web.annotation.controller.SocketController;
import org.elpis.reactive.websockets.web.annotation.controller.SocketMapping;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({"org.elpis.reactive.websockets.web.annotation.controller.SocketController"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class WebSocketHandlerAutoProcessor extends AbstractProcessor {
    private final Random random = new Random();
    private final Set<WebHandlerResourceDescriptor> descriptors = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SocketController.class)) {
            final SocketController socketController = element.getAnnotation(SocketController.class);
            this.createMappings(element, socketController);
        }

        if (roundEnv.processingOver()) {
            this.descriptors.stream()
                    .map(this::getClassDefinition)
                    .map(classBuilder -> JavaFile.builder("org.elpis.reactive.websocket.generated",
                            classBuilder.build()).build())
                    .forEach(javaFile -> {
                        try {
                            javaFile.writeTo(processingEnv.getFiler());
                        } catch (IOException e) {
                            throw new WebSocketConfigurationException("Cannot initiate new class: %s", e.getMessage());
                        }
                    });
        }

        return true;
    }

    private void createMappings(final Element element, final SocketController socketController) {
        element.getEnclosedElements()
                .stream()
                .filter(classElement -> classElement.getKind() == ElementKind.METHOD)
                .filter(classElement -> classElement.getAnnotation(SocketMapping.class) != null)
                .map(ExecutableElement.class::cast)
                .forEach(method -> {
                    WebHandlerResourceDescriptor descriptor = this.configure(socketController, method, element);
                    this.descriptors.add(descriptor);
                });
    }

    private TypeSpec.Builder getClassDefinition(final WebHandlerResourceDescriptor descriptor) {
        final FieldSpec injectedField = FieldSpec.builder(TypeName.get(descriptor.getClazz().asType()), "socketResource")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        final MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Autowired.class)
                .addParameter(TypeName.get(WebSessionRegistry.class), "registry")
                .addParameter(TypeName.get(JsonMapper.class), "jsonMapper")
                .addParameter(TypeName.get(descriptor.getClazz().asType()), "socketResource")
                .addStatement("super(registry, jsonMapper, $S, $L, $L)", descriptor.getPathTemplate(),
                        descriptor.isPingPongEnabled(), descriptor.getPingPongInterval())
                .addStatement("this.socketResource = socketResource")
                .build();

        final MethodSpec suitableMethod = this.getSuitableMethod(descriptor);

        return TypeSpec.classBuilder("WebSocketHandler$" + descriptor.getClazz().getSimpleName().toString() +
                        "$Generated_" + this.randomPostfix())
                .superclass(ClassName.get(this.getHandlerType(descriptor.getMode())))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Component.class)
                .addField(injectedField)
                .addMethod(constructor)
                .addMethod(suitableMethod);
    }

    private MethodSpec getSuitableMethod(WebHandlerResourceDescriptor descriptor) {
        final MethodSpec.Builder methodBuilder = this.getMethodSpec(descriptor);

        final Map<String, Optional<CodeBlock>> codeBlocks = descriptor.getMethod().getParameters()
                .stream()
                .collect(Collectors.toMap(parameter -> parameter.getSimpleName().toString(), parameter ->
                        SocketAnnotationResolverFactory.findResolver(parameter)
                                .map(resolver -> resolver.apply(processingEnv.getElementUtils(),
                                        processingEnv.getTypeUtils()))
                                .map(resolver -> resolver.resolve(parameter))));

        final List<Object> parameters = new ArrayList<>();
        parameters.add(descriptor.getMethod().getSimpleName().toString());

        final List<String> parameterPlaces = new ArrayList<>();

        descriptor.getMethod().getParameters()
                .forEach(parameter -> {
                    parameterPlaces.add("$L");

                    if (codeBlocks.get(parameter.getSimpleName().toString()).isPresent()) {
                        parameters.add(parameter.getSimpleName().toString());
                    } else {
                        parameters.add(TypeUtils.getDefaultValueForType(parameter.asType().getKind()));
                    }
                });

        codeBlocks.values().forEach(codeBlock -> codeBlock.ifPresent(methodBuilder::addCode));

        final String code = (descriptor.useReturn ? "return " : "") +
                "this.socketResource.$L(" + String.join(",", parameterPlaces) + ");";

        return methodBuilder.addCode(code, parameters.toArray())
                .build();
    }

    private MethodSpec.Builder getMethodSpec(WebHandlerResourceDescriptor descriptor) {
        final TypeName fluxMessages = ParameterizedTypeName.get(ClassName.get(Flux.class),
                TypeName.get(WebSocketMessage.class));

        if (descriptor.isUseReturn()) {
            final TypeName publisherWildcard = ParameterizedTypeName.get(ClassName.get(Publisher.class),
                    WildcardTypeName.subtypeOf(Object.class));

            return MethodSpec.methodBuilder("apply")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(WebSocketSessionContext.class, "context")
                    .addParameter(fluxMessages, "messages")
                    .returns(publisherWildcard);
        } else {
            return MethodSpec.methodBuilder("run")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(WebSocketSessionContext.class, "context")
                    .addParameter(fluxMessages, "messages")
                    .returns(void.class);
        }
    }

    private Class<?> getHandlerType(final Mode mode) {
        switch (mode) {
            case SHARED:
                return BroadcastWebSocketResourceHandler.class;
        }

        throw new WebSocketConfigurationException("Cannot find WebSocketHandler implementation for mode %s", mode);
    }

    private WebHandlerResourceDescriptor configure(final SocketController resource,
                                                   final ExecutableElement method,
                                                   final Element clazz) {

        final Element publisher = processingEnv.getElementUtils().getTypeElement(Publisher.class.getCanonicalName());
        final SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);
        final PingPong pingPong = socketMapping.pingPong();
        final String pathTemplate = resource.value() + socketMapping.value();
        final Optional<WebHandlerResourceDescriptor> maybeExistingDescriptor = this.descriptors.stream()
                .filter(handlerResourceDescriptor -> handlerResourceDescriptor.getPathTemplate().equals(pathTemplate))
                .findFirst();

        if (maybeExistingDescriptor.isPresent()) {
            final WebHandlerResourceDescriptor descriptor = maybeExistingDescriptor.get();
            throw new WebSocketConfigurationException("Cannot register method `@SocketMapping %s()` on `%s` since `@SocketMapping %s()` " +
                    "was already registered on provided path", method.getSimpleName(), pathTemplate,
                    descriptor.getMethod().getSimpleName());
        }

        final WebHandlerResourceDescriptor descriptor = new WebHandlerResourceDescriptor(method, clazz,
                method.getReturnType().getKind() != TypeKind.VOID, pathTemplate, socketMapping.mode(),
                pingPong.enabled(), pingPong.value());

        final TypeMirror returnType = method.getReturnType();

        if (descriptor.isUseReturn() && (!processingEnv.getTypeUtils()
                .isAssignable(processingEnv.getTypeUtils().erasure(returnType),
                        processingEnv.getTypeUtils().erasure(publisher.asType())))) {

            throw new WebSocketConfigurationException("Cannot register method `@SocketMapping %s()`. Reason: method should " +
                    "return any of implementation Publisher type. Found `%s`", method.getSimpleName(), method.getReturnType());
        }

        return descriptor;
    }

    private String randomPostfix() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRST".toCharArray();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }

    private static final class WebHandlerResourceDescriptor {
        private final ExecutableElement method;
        private final Element clazz;
        private final boolean useReturn;
        private final String pathTemplate;
        private final Mode mode;
        private final boolean pingPongEnabled;
        private final long pingPongInterval;

        public WebHandlerResourceDescriptor(ExecutableElement method, Element clazz,
                                            boolean useReturn, String pathTemplate, Mode mode,
                                            boolean pingPongEnabled, long pingPongInterval) {

            this.method = method;
            this.clazz = clazz;
            this.useReturn = useReturn;
            this.pathTemplate = pathTemplate;
            this.mode = mode;
            this.pingPongEnabled = pingPongEnabled;
            this.pingPongInterval = pingPongInterval;
        }

        public Element getClazz() {
            return clazz;
        }

        public ExecutableElement getMethod() {
            return method;
        }

        public boolean isUseReturn() {
            return useReturn;
        }

        public String getPathTemplate() {
            return pathTemplate;
        }

        public Mode getMode() {
            return mode;
        }

        public boolean isPingPongEnabled() {
            return pingPongEnabled;
        }

        public long getPingPongInterval() {
            return pingPongInterval;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WebHandlerResourceDescriptor that = (WebHandlerResourceDescriptor) o;
            return Objects.equals(pathTemplate, that.pathTemplate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pathTemplate);
        }

    }


}