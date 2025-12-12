package io.github.elpis.reactive.websockets.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.github.elpis.reactive.websockets.config.Mode;
import io.github.elpis.reactive.websockets.processor.exception.WebSocketProcessorException;
import io.github.elpis.reactive.websockets.processor.flowcontrol.BackpressureFlowController;
import io.github.elpis.reactive.websockets.processor.flowcontrol.HeartbeatFlowController;
import io.github.elpis.reactive.websockets.processor.flowcontrol.RateLimitFlowController;
import io.github.elpis.reactive.websockets.processor.resolver.SocketAnnotationResolverFactory;
import io.github.elpis.reactive.websockets.session.WebSocketSessionContext;
import io.github.elpis.reactive.websockets.util.TypeUtils;
import io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint;
import io.github.elpis.reactive.websockets.web.annotation.OnMessage;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

@SupportedAnnotationTypes({"io.github.elpis.reactive.websockets.web.annotation.MessageEndpoint"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class WebSocketHandlerAutoProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
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

    final MethodSpec constructor =
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
                ParameterSpec.builder(
                        ClassName.bestGuess(
                            "io.github.elpis.reactive.websockets.handler.ratelimit.RateLimiterService"),
                        "rateLimiterService")
                    .addAnnotation(
                        AnnotationSpec.builder(Autowired.class)
                            .addMember("required", "false")
                            .build())
                    .build())
            .addParameter(TypeName.get(descriptor.clazz().asType()), "socketResource")
            .addStatement(
                "super(eventFactory, sessionRegistry, rateLimiterService, $S, $L, $L, $L)",
                descriptor.pathTemplate(),
                generateHeartbeatConfig(descriptor),
                generateRateLimitConfig(descriptor),
                generateBackpressureConfig(descriptor))
            .addStatement("this.socketResource = socketResource")
            .build();

    final MethodSpec suitableMethod = this.getSuitableMethod(descriptor);

    return TypeSpec.classBuilder("WebSocketHandler$Generated_" + descriptor.getPostfix())
        .superclass(ClassName.bestGuess(this.getHandlerType(descriptor.mode())))
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addAnnotation(Component.class)
        .addField(injectedField)
        .addMethod(constructor)
        .addMethod(suitableMethod);
  }

  private MethodSpec getSuitableMethod(WebHandlerResourceDescriptor descriptor) {
    final MethodSpec.Builder methodBuilder = this.getMethodSpec(descriptor);

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

    final List<Object> parameters = new ArrayList<>();
    parameters.add(descriptor.method().getSimpleName().toString());

    final List<String> parameterPlaces = new ArrayList<>();

    descriptor
        .method()
        .getParameters()
        .forEach(
            parameter -> {
              parameterPlaces.add("$L");

              if (codeBlocks.get(parameter.getSimpleName().toString()).isPresent()) {
                parameters.add(parameter.getSimpleName().toString());
              } else {
                parameters.add(TypeUtils.getDefaultValueForType(parameter.asType().getKind()));
              }
            });

    codeBlocks.values().forEach(codeBlock -> codeBlock.ifPresent(methodBuilder::addCode));

    final String methodSignature =
        "this.socketResource.$L(" + String.join(",", parameterPlaces) + ");";

    final String code = (descriptor.useReturn ? "return " : "") + methodSignature;
    return methodBuilder.addCode(code, parameters.toArray()).build();
  }

  private MethodSpec.Builder getMethodSpec(WebHandlerResourceDescriptor descriptor) {
    final TypeName fluxMessages =
        ParameterizedTypeName.get(ClassName.get(Flux.class), TypeName.get(WebSocketMessage.class));

    if (descriptor.useReturn()) {
      final TypeName publisherWildcard =
          ParameterizedTypeName.get(
              ClassName.get(Publisher.class), WildcardTypeName.subtypeOf(Object.class));

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
    return switch (mode) {
      case BROADCAST ->
          "io.github.elpis.reactive.websockets.handler.BroadcastWebSocketResourceHandler";
      case SESSION -> "io.github.elpis.reactive.websockets.handler.SessionWebSocketResourceHandler";
    };
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

    final WebHandlerResourceDescriptor descriptor =
        new WebHandlerResourceDescriptor(
            method,
            clazz,
            method.getReturnType().getKind() != TypeKind.VOID,
            pathTemplate,
            onMessage.mode(),
            heartbeatConfig,
            rateLimitConfig,
            backpressureConfig);

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
      Mode mode,
      HeartbeatConfigData heartbeatConfig,
      RateLimitConfigData rateLimitConfig,
      BackpressureConfigData backpressureConfig) {

    private String getPostfix() {
      final String uniqueKey =
          pathTemplate
              + "."
              + clazz.getSimpleName().toString()
              + "."
              + method.getSimpleName().toString()
              + "."
              + method.getParameters().stream()
                  .map(parameter -> parameter.asType().toString())
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
