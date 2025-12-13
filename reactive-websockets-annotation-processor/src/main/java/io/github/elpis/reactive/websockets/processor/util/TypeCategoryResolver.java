package io.github.elpis.reactive.websockets.processor.util;

import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.springframework.web.reactive.socket.WebSocketMessage;

/**
 * Utility class to categorize types for WebSocket message deserialization.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
public final class TypeCategoryResolver {

  /**
   * Category of type for deserialization strategy selection.
   *
   * @since 1.0.0
   */
  public enum TypeCategory {
    /** WebSocketMessage type - backward compatibility, pass-through */
    WEBSOCKET_MESSAGE,
    /**
     * Simple types handled by TypeUtils - primitives, wrappers, String, BigDecimal, BigInteger,
     * Enum
     */
    SIMPLE_TYPE,
    /** Complex types handled by JsonMapper - POJOs, Collections, Maps */
    COMPLEX_TYPE
  }

  /**
   * Set of simple type names that can be converted using TypeUtils.
   *
   * @since 1.0.0
   */
  private static final Set<String> SIMPLE_TYPE_NAMES =
      Set.of(
          // Wrappers
          "java.lang.Integer",
          "java.lang.Long",
          "java.lang.Short",
          "java.lang.Byte",
          "java.lang.Double",
          "java.lang.Float",
          "java.lang.Boolean",
          "java.lang.Character",
          // Primitives are handled separately via TypeKind
          // Big Numbers
          "java.math.BigDecimal",
          "java.math.BigInteger",
          // String
          "java.lang.String");

  private TypeCategoryResolver() {}

  /**
   * Categorizes a type for deserialization strategy selection.
   *
   * @param type the type to categorize
   * @param elements the Elements utility
   * @param types the Types utility
   * @return the category of the type
   * @since 1.0.0
   */
  public static TypeCategory categorize(
      final TypeMirror type, final Elements elements, final Types types) {
    // Check for WebSocketMessage
    if (isWebSocketMessage(type, elements, types)) {
      return TypeCategory.WEBSOCKET_MESSAGE;
    }

    // Check for simple types
    if (isSimpleType(type, elements, types)) {
      return TypeCategory.SIMPLE_TYPE;
    }

    // Default to complex type
    return TypeCategory.COMPLEX_TYPE;
  }

  /**
   * Checks if the type is a WebSocketMessage.
   *
   * @param type the type to check
   * @param elements the Elements utility
   * @param types the Types utility
   * @return true if the type is WebSocketMessage
   * @since 1.0.0
   */
  private static boolean isWebSocketMessage(
      final TypeMirror type, final Elements elements, final Types types) {
    final TypeElement webSocketMessageType =
        elements.getTypeElement(WebSocketMessage.class.getCanonicalName());
    return types.isSameType(types.erasure(type), types.erasure(webSocketMessageType.asType()));
  }

  /**
   * Checks if the type is a simple type that can be converted using TypeUtils.
   *
   * @param type the type to check
   * @param elements the Elements utility
   * @param types the Types utility
   * @return true if the type is a simple type
   * @since 1.0.0
   */
  public static boolean isSimpleType(
      final TypeMirror type, final Elements elements, final Types types) {
    // Check primitives
    if (type.getKind().isPrimitive()) {
      return true;
    }

    // Check against simple type names
    final String typeName = type.toString();
    if (SIMPLE_TYPE_NAMES.contains(typeName)) {
      return true;
    }

    // Check if Enum
    final Element typeElement = types.asElement(type);
    if (typeElement instanceof TypeElement te) {
      final TypeElement enumType = elements.getTypeElement("java.lang.Enum");
      if (enumType != null) {
        return types.isSubtype(types.erasure(te.asType()), types.erasure(enumType.asType()));
      }
    }

    return false;
  }
}
