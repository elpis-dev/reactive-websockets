package io.github.elpis.reactive.websockets.processor.resolver;

import io.github.elpis.reactive.websockets.processor.exception.ExceptionHandlerInfo;
import io.github.elpis.reactive.websockets.web.annotation.WebSocketAdvice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Resolves {@code @ExceptionHandler} methods from {@code @MessageEndpoint} and
 * {@code @WebSocketAdvice} classes during annotation processing.
 *
 * <p>This resolver:
 *
 * <ul>
 *   <li>Scans classes for {@code @ExceptionHandler} annotated methods
 *   <li>Extracts exception types from annotation values or method parameters
 *   <li>Validates method signatures and type compatibility at compile-time
 *   <li>Creates {@code ExceptionHandlerInfo} objects for code generation
 * </ul>
 *
 * <p><b>Compile-Time Validation:</b> This resolver performs strict validation to prevent runtime
 * {@code ClassCastException} errors. It ensures that all exception types in the annotation are
 * assignable to the method parameter type.
 *
 * @since 1.0.0
 */
public class ExceptionHandlerResolver {

  /**
   * Discovers all @WebSocketAdvice classes in the current compilation round and extracts their
   * exception handlers.
   *
   * @param roundEnv the round environment
   * @param elements the Elements utility
   * @param types the Types utility
   * @return map of @WebSocketAdvice TypeElement to their exception handlers
   */
  public static Map<TypeElement, List<ExceptionHandlerInfo>> discoverGlobalHandlers(
      RoundEnvironment roundEnv, Elements elements, Types types) {

    Map<TypeElement, List<ExceptionHandlerInfo>> globalHandlers = new HashMap<>();

    Set<? extends Element> webSocketAdviceElements =
        roundEnv.getElementsAnnotatedWith(WebSocketAdvice.class);
    for (Element element : webSocketAdviceElements) {
      if (element.getKind() == ElementKind.CLASS) {
        TypeElement adviceClass = (TypeElement) element;
        List<ExceptionHandlerInfo> handlers = resolveHandlers(adviceClass, elements, types, false);
        if (!handlers.isEmpty()) {
          globalHandlers.put(adviceClass, handlers);
        }
      }
    }

    return globalHandlers;
  }

  /**
   * Resolves local exception handlers from a @MessageEndpoint class.
   *
   * @param messageEndpointClass the @MessageEndpoint type element
   * @param elements the Elements utility
   * @param types the Types utility
   * @return list of exception handler info objects
   */
  public static List<ExceptionHandlerInfo> resolveLocalHandlers(
      Element messageEndpointClass, Elements elements, Types types) {

    return resolveHandlers(messageEndpointClass, elements, types, true);
  }

  /**
   * Resolves exception handlers from any class.
   *
   * @param classElement the class element to scan
   * @param elements the Elements utility
   * @param types the Types utility
   * @param isLocal true if handlers are local (in @MessageEndpoint), false if global
   * @return list of exception handler info objects
   */
  public static List<ExceptionHandlerInfo> resolveHandlers(
      Element classElement, Elements elements, Types types, boolean isLocal) {

    List<ExceptionHandlerInfo> handlers = new ArrayList<>();
    TypeElement beanClass = isLocal ? null : (TypeElement) classElement;

    for (Element enclosed : classElement.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.METHOD) {
        ExecutableElement method = (ExecutableElement) enclosed;
        ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);

        if (annotation != null) {
          try {
            ExceptionHandlerInfo info =
                createHandlerInfo(method, annotation, elements, types, isLocal, beanClass);
            handlers.add(info);
          } catch (Exception ex) {
            throw new IllegalStateException(
                "Invalid @ExceptionHandler method: "
                    + method.getSimpleName()
                    + " in "
                    + classElement.getSimpleName()
                    + ": "
                    + ex.getMessage(),
                ex);
          }
        }
      }
    }

    return handlers;
  }

  /** Creates an ExceptionHandlerInfo from a method and its annotation. */
  private static ExceptionHandlerInfo createHandlerInfo(
      ExecutableElement method,
      ExceptionHandler annotation,
      Elements elements,
      Types types,
      boolean isLocal,
      TypeElement beanClass) {

    List<TypeMirror> exceptionTypes = extractExceptionTypes(method, annotation);
    validateHandlerMethod(method, exceptionTypes, elements, types);

    TypeMirror returnType = method.getReturnType();
    boolean returnsVoid = returnType.getKind() == TypeKind.VOID;

    return new ExceptionHandlerInfo(
        method, exceptionTypes, returnsVoid, returnType, isLocal, beanClass);
  }

  /**
   * Extracts exception types from @ExceptionHandler annotation value. Supports both single and
   * multiple exception types: - @ExceptionHandler (uses first method parameter)
   * - @ExceptionHandler(CustomException.class) - @ExceptionHandler({CustomException1.class,
   * CustomException2.class})
   */
  private static List<TypeMirror> extractExceptionTypes(
      ExecutableElement method, ExceptionHandler annotation) {

    try {
      annotation.value();
      return inferFromParameter(method);
    } catch (javax.lang.model.type.MirroredTypeException e) {
      return List.of(e.getTypeMirror());
    } catch (javax.lang.model.type.MirroredTypesException e) {
      List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
      if (!typeMirrors.isEmpty()) {
        return new ArrayList<>(typeMirrors);
      }
      return inferFromParameter(method);
    }
  }

  /** Infers exception type from the first method parameter. */
  private static List<TypeMirror> inferFromParameter(ExecutableElement method) {
    if (method.getParameters().isEmpty()) {
      throw new IllegalArgumentException(
          "@ExceptionHandler method must have at least one parameter: " + method.getSimpleName());
    }

    VariableElement firstParam = method.getParameters().get(0);
    TypeMirror paramType = firstParam.asType();

    return List.of(paramType);
  }

  /**
   * Validates the exception handler method signature and type compatibility.
   *
   * <p>Validation rules: 1. Method must have at least one parameter 2. First parameter must extend
   * Throwable 3. If @ExceptionHandler has values, each exception type must be assignable to the
   * method parameter type (prevents ClassCastException) 4. If @ExceptionHandler is empty, infer
   * from first parameter
   *
   * @throws IllegalArgumentException with descriptive message if validation fails (causes
   *     compilation to fail)
   */
  private static void validateHandlerMethod(
      ExecutableElement method, List<TypeMirror> exceptionTypes, Elements elements, Types types) {

    List<? extends VariableElement> parameters = method.getParameters();

    if (parameters.isEmpty()) {
      throw new IllegalArgumentException(
          "@ExceptionHandler method must have at least one parameter extending Throwable. "
              + "Add a parameter like 'Exception ex' or 'RuntimeException ex'");
    }

    TypeMirror paramType = parameters.get(0).asType();

    TypeElement throwableElement = elements.getTypeElement("java.lang.Throwable");
    if (throwableElement != null) {
      TypeMirror throwableType = throwableElement.asType();
      if (!types.isAssignable(paramType, throwableType)) {
        throw new IllegalArgumentException(
            "First parameter of @ExceptionHandler must extend Throwable, but was: " + paramType);
      }
    }

    validateExceptionTypeCompatibility(exceptionTypes, paramType, method, types);
  }

  /**
   * Validates that all exception types are assignable to the parameter type.
   *
   * <p>Example validations: - VALID: @ExceptionHandler(IllegalArgumentException.class) + parameter
   * RuntimeException - INVALID: @ExceptionHandler(RuntimeException.class) + parameter
   * IllegalArgumentException - INVALID: @ExceptionHandler(IOException.class) + parameter
   * ValidationException
   *
   * @throws IllegalArgumentException if any exception type is incompatible with parameter
   */
  private static void validateExceptionTypeCompatibility(
      List<TypeMirror> exceptionTypes,
      TypeMirror parameterType,
      ExecutableElement method,
      Types types) {

    for (TypeMirror exceptionType : exceptionTypes) {
      // Check if exceptionType is assignable to parameterType
      // This means: can we assign an instance of exceptionType to a variable of parameterType?
      if (!types.isAssignable(exceptionType, parameterType)) {
        throw new IllegalArgumentException(
            String.format(
                "Exception type '%s' in @ExceptionHandler is not assignable to parameter type '%s' in method '%s'. "
                    + "This would cause ClassCastException at runtime. "
                    + "Fix: Change parameter to '%s ex' or use @ExceptionHandler(%s.class)",
                exceptionType,
                parameterType,
                method.getSimpleName(),
                exceptionType,
                parameterType));
      }
    }
  }
}
