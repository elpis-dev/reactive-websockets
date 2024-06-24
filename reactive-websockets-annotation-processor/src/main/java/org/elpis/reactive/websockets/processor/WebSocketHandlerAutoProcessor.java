package org.elpis.reactive.websockets.processor;

import com.squareup.javapoet.*;
import org.elpis.reactive.websockets.config.model.Mode;
import org.elpis.reactive.websockets.config.model.WebSocketSessionContext;
import org.elpis.reactive.websockets.processor.exception.WebSocketProcessorException;
import org.elpis.reactive.websockets.processor.resolver.SocketAnnotationResolverFactory;
import org.elpis.reactive.websockets.util.TypeUtils;
import org.elpis.reactive.websockets.web.annotation.PingPong;
import org.elpis.reactive.websockets.web.annotation.SocketController;
import org.elpis.reactive.websockets.web.annotation.SocketMapping;
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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({"org.elpis.reactive.websockets.web.annotation.SocketController"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class WebSocketHandlerAutoProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SocketController.class)) {
            final SocketController socketController = element.getAnnotation(SocketController.class);
            this.createMapping(element, socketController)
                    .stream()
                    .map(this::getClassDefinition)
                    .map(classBuilder -> JavaFile.builder("org.elpis.reactive.websockets.generated",
                            classBuilder.build()).build())
                    .forEach(javaFile -> {
                        try {
                            javaFile.writeTo(processingEnv.getFiler());
                        } catch (IOException e) {
                            throw new WebSocketProcessorException("Cannot initiate new class: %s", e.getMessage());
                        }
                    });
        }

        return true;
    }

    private List<WebHandlerResourceDescriptor> createMapping(final Element element, final SocketController socketController) {
        return element.getEnclosedElements()
                .stream()
                .filter(classElement -> classElement.getKind() == ElementKind.METHOD)
                .filter(classElement -> classElement.getAnnotation(SocketMapping.class) != null)
                .map(ExecutableElement.class::cast)
                .map(method -> this.configure(socketController, method, element))
                .collect(Collectors.toList());
    }

    private TypeSpec.Builder getClassDefinition(final WebHandlerResourceDescriptor descriptor) {
        final FieldSpec injectedField = FieldSpec.builder(TypeName.get(descriptor.getClazz().asType()), "socketResource")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        final MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Autowired.class)
                .addParameter(ClassName.bestGuess("org.elpis.reactive.websockets.config.registry.WebSessionRegistry"), "registry")
                .addParameter(ClassName.bestGuess("org.elpis.reactive.websockets.mapper.JsonMapper"), "jsonMapper")
                .addParameter(TypeName.get(descriptor.getClazz().asType()), "socketResource")
                .addStatement("super(registry, jsonMapper, $S, $L, $L)", descriptor.getPathTemplate(),
                        descriptor.isPingPongEnabled(), descriptor.getPingPongInterval())
                .addStatement("this.socketResource = socketResource")
                .build();

        final MethodSpec suitableMethod = this.getSuitableMethod(descriptor);

        return TypeSpec.classBuilder("WebSocketHandler$Generated_" + descriptor.getPostfix())
                .superclass(ClassName.bestGuess(this.getHandlerType(descriptor.getMode())))
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

        final String methodSignature = "this.socketResource.$L(" + String.join(",", parameterPlaces) + ");";

        final String code = (descriptor.useReturn ? "return " : "") + methodSignature;
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

    private String getHandlerType(final Mode mode) {
        switch (mode) {
            case SHARED:
                return "org.elpis.reactive.websockets.config.handler.BroadcastWebSocketResourceHandler";
        }

        throw new WebSocketProcessorException("Cannot find WebSocketHandler implementation for mode %s", mode);
    }

    private WebHandlerResourceDescriptor configure(final SocketController resource,
                                                   final ExecutableElement method,
                                                   final Element clazz) {

        final Element publisher = processingEnv.getElementUtils().getTypeElement(Publisher.class.getCanonicalName());
        final SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);
        final PingPong pingPong = socketMapping.pingPong();
        final String pathTemplate = resource.value() + socketMapping.value();

        final WebHandlerResourceDescriptor descriptor = new WebHandlerResourceDescriptor(method, clazz,
                method.getReturnType().getKind() != TypeKind.VOID, pathTemplate, socketMapping.mode(),
                pingPong.enabled(), pingPong.value());

        final TypeMirror returnType = method.getReturnType();

        if (descriptor.isUseReturn() && (!processingEnv.getTypeUtils()
                .isAssignable(processingEnv.getTypeUtils().erasure(returnType),
                        processingEnv.getTypeUtils().erasure(publisher.asType())))) {

            throw new WebSocketProcessorException("Cannot register method `@SocketMapping %s()`. Reason: method should " +
                    "return any of implementation Publisher type. Found `%s`", method.getSimpleName(), method.getReturnType());
        }

        return descriptor;
    }

    private String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString().toLowerCase();
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

        private String getPostfix() {
            final String uniqueKey = pathTemplate + "." + clazz.getSimpleName().toString() +
                    "." + method.getSimpleName().toString() + "." + method.getParameters().stream().map(parameter -> parameter.asType().toString())
                    .collect(Collectors.joining(","));
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(uniqueKey.getBytes());
                return this.toHexString(hash);
            } catch (NoSuchAlgorithmException e) {
                throw new WebSocketProcessorException(e.getMessage());
            }
        }

        private String toHexString(byte[] hash) {
            BigInteger number = new BigInteger(1, hash);

            StringBuilder hexString = new StringBuilder(number.toString(16));

            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        }

    }


}