package io.github.elpis.reactive.websockets.processor;

import static io.github.elpis.reactive.websockets.processor.util.Constants.VARIABLE_SUFFIX;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.github.elpis.reactive.websockets.processor.codegen.ExceptionHandlerCodeGenerator;
import io.github.elpis.reactive.websockets.processor.exception.ExceptionHandlerInfo;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketProcessorException;
import io.github.elpis.reactive.websockets.processor.flowcontrol.BackpressureFlowController;
import io.github.elpis.reactive.websockets.processor.flowcontrol.HeartbeatFlowController;
import io.github.elpis.reactive.websockets.processor.flowcontrol.RateLimitFlowController;
import io.github.elpis.reactive.websockets.processor.resolver.ExceptionHandlerResolver;
import io.github.elpis.reactive.websockets.processor.resolver.SocketAnnotationResolverFactory;
import io.github.elpis.reactive.websockets.processor.util.HashUtils;
import io.github.elpis.reactive.websockets.util.TypeUtils;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Annotation processor that generates WebSocket handler classes from {@code @MessageEndpoint}
 * annotated classes.
 *
 * <p>This processor scans for classes annotated with {@code @MessageEndpoint} and generates
 * corresponding handler implementations that extend {@code AdaptiveWebSocketHandler}.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * @MessageEndpoint("/ws/chat")
 * public class ChatEndpoint {
 *
 *     @OnMessage("/room/{roomId}")
 *     public Flux<String> handleMessage(@PathVariable String roomId,
 *                                        Flux<WebSocketMessage> messages) {
 *         return messages.map(msg -> "Echo: " + msg.getPayloadAsText());
 *     }
 * }
 * }</pre>
 *
 * <p>The processor generates a handler class that:
 *
 * <ul>
 *   <li>Extends {@code AdaptiveWebSocketHandler} for flow control support
 *   <li>Injects the original endpoint as a Spring bean
 *   <li>Implements {@code processMessages()} to delegate to the user's handler method
 *   <li>Supports heartbeat, rate limiting, and backpressure configurations
 * </ul>
 *
 * @see io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint
 * @see io.github.elpis.reactive.websockets.web.annotation.OnMessage
 */
@SupportedAnnotationTypes({
  "io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint",
  "io.github.elpis.reactive.websockets.web.annotation.WebSocketAdvice"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class WebSocketHandlerAutoProcessor extends AbstractProcessor {
  private Map<TypeElement, List<ExceptionHandlerInfo>> globalHandlers;

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (globalHandlers == null) {
      globalHandlers =
          ExceptionHandlerResolver.discoverGlobalHandlers(
              roundEnv, processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    for (Element element : roundEnv.getElementsAnnotatedWith(MessageEndpoint.class)) {
      final MessageEndpoint messageEndpoint = element.getAnnotation(MessageEndpoint.class);
      this.createMapping(element, messageEndpoint).stream()
          .map(this::getClassDefinition)
          .map(
              classBuilder ->
                  JavaFile.builder(
                          "io.github.elpis.reactive.websockets.generated", classBuilder.build())
                      .build())
          .forEach(
              javaFile -> {
                try {
                  javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                  throw new WebSocketProcessorException(
                      "Cannot initiate new class: %s", e.getMessage());
                }
              });
    }

    return true;
  }

  private List<WebHandlerResourceDescriptor> createMapping(
      final Element element, final MessageEndpoint messageEndpoint) {
    return element.getEnclosedElements().stream()
        .filter(classElement -> classElement.getKind() == ElementKind.METHOD)
        .filter(classElement -> classElement.getAnnotation(OnMessage.class) != null)
        .map(ExecutableElement.class::cast)
        .map(method -> this.configure(messageEndpoint, method, element))
        .toList();
  }

  private TypeSpec.Builder getClassDefinition(final WebHandlerResourceDescriptor descriptor) {
    final FieldSpec injectedField =
        FieldSpec.builder(TypeName.get(descriptor.clazz().asType()), "socketResource")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Autowired.class)
            .addParameter(
                ClassName.bestGuess(
                    "io.github.elpis.reactive.websockets.event.manager.WebSocketEventManagerFactory"),
                "eventFactory")
            .addParameter(
                ClassName.bestGuess(
                    "io.github.elpis.reactive.websockets.session.WebSocketSessionRegistry"),
                "sessionRegistry")
            .addParameter(
                ClassName.bestGuess(
                    "io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService"),
                "rateLimiterService")
            .addParameter(TypeName.get(descriptor.clazz().asType()), "socketResource");

    List<FieldSpec> globalHandlerFields = new ArrayList<>();
    for (TypeElement adviceClass : globalHandlers.keySet()) {
      String fieldName = getGlobalHandlerFieldName(adviceClass);
      TypeName typeName = TypeName.get(adviceClass.asType());

      constructorBuilder.addParameter(typeName, fieldName);

      FieldSpec field =
          FieldSpec.builder(typeName, fieldName)
              .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
              .build();
      globalHandlerFields.add(field);
    }

    constructorBuilder
        .addStatement(
            "super(eventFactory, sessionRegistry, $S, $L, $L, $L, rateLimiterService)",
            descriptor.pathTemplate(),
            generateHeartbeatConfig(descriptor),
            generateRateLimitConfig(descriptor),
            generateBackpressureConfig(descriptor))
        .addStatement("this.socketResource = socketResource");

    for (TypeElement adviceClass : globalHandlers.keySet()) {
      String fieldName = getGlobalHandlerFieldName(adviceClass);
      constructorBuilder.addStatement("this.$L = $L", fieldName, fieldName);
    }

    final MethodSpec constructor = constructorBuilder.build();
    final MethodSpec suitableMethod = this.getSuitableMethod(descriptor);

    TypeSpec.Builder classBuilder =
        TypeSpec.classBuilder("WebSocketHandler$Generated_" + descriptor.getPostfix())
            .superclass(
                ClassName.bestGuess(
                    "io.github.elpis.reactive.websockets.handler.AdaptiveWebSocketHandler"))
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(Component.class)
            .addField(injectedField)
            .addMethod(constructor)
            .addMethod(suitableMethod);

    globalHandlerFields.forEach(classBuilder::addField);

    return classBuilder;
  }

  private String getGlobalHandlerFieldName(TypeElement typeElement) {
    String className = typeElement.getSimpleName().toString();
    return Character.toLowerCase(className.charAt(0)) + className.substring(1);
  }

  private MethodSpec getSuitableMethod(WebHandlerResourceDescriptor descriptor) {
    final MethodSpec.Builder methodBuilder = this.getMethodSpec(descriptor);

    // Apply rate limiting if enabled, otherwise just get messages from inbound flux
    if (descriptor.rateLimitConfig() != null) {
      methodBuilder.addStatement(
          "final $T<$T> messages = this.applyRateLimit(streams.inboundFlux(), context)",
          ClassName.get("reactor.core.publisher", "Flux"),
          ClassName.get("org.springframework.web.reactive.socket", "WebSocketMessage"));
    } else {
      methodBuilder.addStatement(
          "final $T<$T> messages = streams.inboundFlux()",
          ClassName.get("reactor.core.publisher", "Flux"),
          ClassName.get("org.springframework.web.reactive.socket", "WebSocketMessage"));
    }

    final Map<String, Optional<CodeBlock>> codeBlocks =
        descriptor.method().getParameters().stream()
            .collect(
                Collectors.toMap(
                    parameter -> parameter.getSimpleName().toString(),
                    parameter ->
                        SocketAnnotationResolverFactory.findResolver(parameter)
                            .map(
                                resolver ->
                                    resolver.apply(
                                        processingEnv.getElementUtils(),
                                        processingEnv.getTypeUtils()))
                            .map(resolver -> resolver.resolve(parameter))));

    codeBlocks.values().forEach(codeBlock -> codeBlock.ifPresent(methodBuilder::addCode));

    final List<Object> parameters = new ArrayList<>();
    parameters.add(descriptor.method().getSimpleName().toString());

    final List<String> parameterPlaces = new ArrayList<>();

    descriptor
        .method()
        .getParameters()
        .forEach(
            parameter -> {
              parameterPlaces.add("$L");

              final Optional<CodeBlock> codeBlock =
                  codeBlocks.get(parameter.getSimpleName().toString());
              if (codeBlock.isPresent()) {
                if (codeBlock.get().isEmpty()) {
                  parameters.add("messages");
                } else {
                  parameters.add(parameter.getSimpleName() + VARIABLE_SUFFIX);
                }
              } else {
                parameters.add(TypeUtils.getDefaultValueForType(parameter.asType().getKind()));
              }
            });

    final String methodSignature =
        "this.socketResource.$L(" + String.join(",", parameterPlaces) + ")";

    if (descriptor.useReturn()) {
      CodeBlock errorHandlerChain =
          ExceptionHandlerCodeGenerator.generateErrorHandlerChain(
              descriptor.localHandlers(), globalHandlers);

      List<Object> fluxFromArgs = new ArrayList<>();
      fluxFromArgs.add(ClassName.get("reactor.core.publisher", "Flux"));
      fluxFromArgs.add(ClassName.get("reactor.core.publisher", "Flux"));
      fluxFromArgs.addAll(parameters);

      methodBuilder
          .addCode(
              "final $T<?> results = $T.from(" + methodSignature + ");\n", fluxFromArgs.toArray())
          .addCode(
              "return results.doOnNext(result -> streams.outboundSink().tryEmitNext(result))\n");

      // Add error handling chain AFTER .doOnNext() to ensure proper type inference
      if (!errorHandlerChain.isEmpty()) {
        methodBuilder.addCode(errorHandlerChain);
      }

      methodBuilder.addCode(";\n");

    } else {
      methodBuilder.addStatement(methodSignature, parameters.toArray());
      methodBuilder.addStatement(
          "return $T.empty()", ClassName.get("reactor.core.publisher", "Mono"));
    }

    return methodBuilder.build();
  }

  private MethodSpec.Builder getMethodSpec(WebHandlerResourceDescriptor descriptor) {
    return MethodSpec.methodBuilder("processMessages")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(
            ClassName.get("io.github.elpis.reactive.websockets.session", "WebSocketSessionContext"),
            "context")
        .addParameter(
            ClassName.get("io.github.elpis.reactive.websockets.session", "SessionStreams"),
            "streams")
        .returns(
            ParameterizedTypeName.get(
                ClassName.get("org.reactivestreams", "Publisher"),
                WildcardTypeName.subtypeOf(Object.class)));
  }

  private WebHandlerResourceDescriptor configure(
      final MessageEndpoint resource, final ExecutableElement method, final Element clazz) {

    final Element publisher =
        processingEnv.getElementUtils().getTypeElement(Publisher.class.getCanonicalName());
    final OnMessage onMessage = method.getAnnotation(OnMessage.class);
    final String pathTemplate = resource.value() + onMessage.value();

    final HeartbeatConfigData heartbeatConfig =
        HeartbeatFlowController.resolveHeartbeatConfig(method, clazz);
    final RateLimitConfigData rateLimitConfig =
        RateLimitFlowController.resolveRateLimitConfig(method, clazz);
    final BackpressureConfigData backpressureConfig =
        BackpressureFlowController.resolveBackpressureConfig(method, clazz);

    final List<ExceptionHandlerInfo> localHandlers =
        ExceptionHandlerResolver.resolveLocalHandlers(
            clazz, processingEnv.getElementUtils(), processingEnv.getTypeUtils());

    final WebHandlerResourceDescriptor descriptor =
        new WebHandlerResourceDescriptor(
            method,
            clazz,
            method.getReturnType().getKind() != TypeKind.VOID,
            pathTemplate,
            heartbeatConfig,
            rateLimitConfig,
            backpressureConfig,
            localHandlers);

    final TypeMirror returnType = method.getReturnType();

    if (descriptor.useReturn()
        && (!processingEnv
            .getTypeUtils()
            .isAssignable(
                processingEnv.getTypeUtils().erasure(returnType),
                processingEnv.getTypeUtils().erasure(publisher.asType())))) {

      throw new WebSocketProcessorException(
          "Cannot register method `@OnMessage %s()`. Reason: method should "
              + "return any of implementation Publisher type. Found `%s`",
          method.getSimpleName(), method.getReturnType());
    }

    return descriptor;
  }

  /**
   * Generates code block for HeartbeatConfig creation. Delegates to {@link
   * HeartbeatFlowController}.
   */
  private String generateHeartbeatConfig(WebHandlerResourceDescriptor descriptor) {
    return HeartbeatFlowController.generateHeartbeatConfig(descriptor.heartbeatConfig());
  }

  /**
   * Generates code block for RateLimitConfig creation. Delegates to {@link
   * RateLimitFlowController}.
   */
  private String generateRateLimitConfig(WebHandlerResourceDescriptor descriptor) {
    return RateLimitFlowController.generateRateLimitConfig(descriptor.rateLimitConfig());
  }

  /**
   * Generates code block for BackpressureConfig creation. Delegates to {@link
   * BackpressureFlowController}.
   */
  private String generateBackpressureConfig(WebHandlerResourceDescriptor descriptor) {
    return BackpressureFlowController.generateBackpressureConfig(descriptor.backpressureConfig());
  }

  public record HeartbeatConfigData(long interval, long timeout) {}

  public record RateLimitConfigData(
      int limitForPeriod, long limitRefreshPeriod, String timeUnit, long timeout, String scope) {}

  public record BackpressureConfigData(String strategy, int bufferSize) {}

  private record WebHandlerResourceDescriptor(
      ExecutableElement method,
      Element clazz,
      boolean useReturn,
      String pathTemplate,
      HeartbeatConfigData heartbeatConfig,
      RateLimitConfigData rateLimitConfig,
      BackpressureConfigData backpressureConfig,
      List<ExceptionHandlerInfo> localHandlers) {

    private String getPostfix() {
      final String parameters =
          method.getParameters().stream()
              .map(parameter -> parameter.asType().toString())
              .collect(Collectors.joining(","));
      return HashUtils.INSTANCE.generateHash(
          pathTemplate,
          clazz.getSimpleName().toString(),
          method.getSimpleName().toString(),
          parameters);
    }
  }
}
