package io.github.elpis.reactive.websockets.util;

import static java.util.Objects.isNull;

import io.github.elpis.reactive.websockets.exception.WebSocketValidationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.UnknownFormatConversionException;
import java.util.function.BiFunction;
import javax.lang.model.type.TypeKind;

/**
 * Utility class to support safe class casting and type converting.
 *
 * @author Phillip J. Fry
 * @since 1.0.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class TypeUtils {

  /**
   * Registry of possible conversions from {@link String} to any supported type.
   *
   * @since 1.0.0
   */
  private static final Map<Class<?>, BiFunction<String, Class<?>, ?>> convertRegistry =
      Map.ofEntries(
          // Wrappers
          Map.entry(Integer.class, (data, type) -> Integer.valueOf(data)),
          Map.entry(Long.class, (data, type) -> Long.valueOf(data)),
          Map.entry(Short.class, (data, type) -> Short.valueOf(data)),
          Map.entry(Byte.class, (data, type) -> Byte.valueOf(data)),
          Map.entry(Double.class, (data, type) -> Double.valueOf(data)),
          Map.entry(Float.class, (data, type) -> Float.valueOf(data)),
          Map.entry(Boolean.class, (data, type) -> Boolean.valueOf(data)),
          Map.entry(Character.class, (data, type) -> data.charAt(0)),

          // Primitives
          Map.entry(int.class, (data, type) -> Integer.valueOf(data)),
          Map.entry(long.class, (data, type) -> Long.valueOf(data)),
          Map.entry(short.class, (data, type) -> Short.valueOf(data)),
          Map.entry(byte.class, (data, type) -> Byte.valueOf(data)),
          Map.entry(double.class, (data, type) -> Double.valueOf(data)),
          Map.entry(float.class, (data, type) -> Float.valueOf(data)),
          Map.entry(boolean.class, (data, type) -> Boolean.valueOf(data)),
          Map.entry(char.class, (data, type) -> data.charAt(0)),

          // Big Numbers
          Map.entry(BigDecimal.class, (data, type) -> new BigDecimal(data)),
          Map.entry(BigInteger.class, (data, type) -> new BigInteger(data)),

          // Objects
          Map.entry(String.class, (data, type) -> data),
          Map.entry(Enum.class, (data, type) -> Enum.valueOf((Class<? extends Enum>) type, data)));

  /**
   * Registry default values for primitives.
   *
   * @since 1.0.0
   */
  private static final Map<Class<?>, ?> defaultValuesRegistry =
      Map.ofEntries(
          Map.entry(boolean.class, false),
          Map.entry(byte.class, 0),
          Map.entry(short.class, 0),
          Map.entry(int.class, 0),
          Map.entry(long.class, 0L),
          Map.entry(char.class, '\u0000'),
          Map.entry(float.class, 0.0f),
          Map.entry(double.class, 0.0d));

  private static final Map<TypeKind, ?> defaultValuesKindRegistry =
      Map.ofEntries(
          Map.entry(TypeKind.BOOLEAN, false),
          Map.entry(TypeKind.BYTE, 0),
          Map.entry(TypeKind.SHORT, 0),
          Map.entry(TypeKind.INT, 0),
          Map.entry(TypeKind.LONG, 0L),
          Map.entry(TypeKind.CHAR, '\u0000'),
          Map.entry(TypeKind.FLOAT, 0.0f),
          Map.entry(TypeKind.DOUBLE, 0.0d));

  private TypeUtils() {}

  /**
   * Returns default value for primitives.
   *
   * @param clazz the primitive class
   * @return default value for primitive
   * @since 1.0.0
   */
  public static <T> T getDefaultValueForType(final Class<T> clazz) {
    if (isNull(clazz)) {
      throw new WebSocketValidationException("Cannot process null source type");
    }

    return (T) defaultValuesRegistry.get(clazz);
  }

  /**
   * Returns default value for primitives.
   *
   * @param type the primitive class
   * @return default value for primitive
   * @since 1.0.0
   */
  public static <T> T getDefaultValueForType(final TypeKind type) {
    if (isNull(type)) {
      throw new WebSocketValidationException("Cannot process null source type");
    }

    return (T) defaultValuesKindRegistry.get(type);
  }

  /**
   * Converts {@link String} to any supported data type.
   *
   * @param data data to convert
   * @param clazz the type to convert to
   * @return conversion result
   * @since 1.0.0
   */
  public static <T> T convert(final String data, final Class<T> clazz) {
    if (isNull(data) || data.isEmpty()) {
      throw new WebSocketValidationException("Cannot convert empty or null source");
    }

    final BiFunction<String, Class<?>, ?> convertFunction =
        Enum.class.isAssignableFrom(clazz)
            ? convertRegistry.get(Enum.class)
            : convertRegistry.get(clazz);

    if (isNull(convertFunction)) {
      throw new UnknownFormatConversionException(
          "Unable to find convert function for type " + clazz.toString());
    }

    return (T) convertFunction.apply(data, clazz);
  }
}
