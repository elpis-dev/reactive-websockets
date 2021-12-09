package org.elpis.reactive.websockets.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.elpis.reactive.websockets.exception.ValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.UnknownFormatConversionException;
import java.util.function.BiFunction;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unchecked", "rawtypes"})
public class TypeUtils {
    private static final Map<Class<?>, BiFunction<String, Class<?>, ?>> convertRegistry = Map.ofEntries(
            //Wrappers
            Map.entry(Integer.class, (data, type) -> Integer.valueOf(data)),
            Map.entry(Long.class, (data, type) -> Long.valueOf(data)),
            Map.entry(Short.class, (data, type) -> Short.valueOf(data)),
            Map.entry(Byte.class, (data, type) -> Byte.valueOf(data)),
            Map.entry(Double.class, (data, type) -> Double.valueOf(data)),
            Map.entry(Float.class, (data, type) -> Float.valueOf(data)),
            Map.entry(Boolean.class, (data, type) -> Boolean.valueOf(data)),
            Map.entry(Character.class, (data, type) -> data.charAt(0)),

            //Primitives
            Map.entry(int.class, (data, type) -> Integer.valueOf(data)),
            Map.entry(long.class, (data, type) -> Long.valueOf(data)),
            Map.entry(short.class, (data, type) -> Short.valueOf(data)),
            Map.entry(byte.class, (data, type) -> Byte.valueOf(data)),
            Map.entry(double.class, (data, type) -> Double.valueOf(data)),
            Map.entry(float.class, (data, type) -> Float.valueOf(data)),
            Map.entry(boolean.class, (data, type) -> Boolean.valueOf(data)),
            Map.entry(char.class, (data, type) -> data.charAt(0)),

            //Big Numbers
            Map.entry(BigDecimal.class, (data, type) -> new BigDecimal(data)),
            Map.entry(BigInteger.class, (data, type) -> new BigInteger(data)),

            //Objects
            Map.entry(String.class, (data, type) -> data),
            Map.entry(Enum.class, (data, type) -> Enum.valueOf((Class<? extends Enum>) type, data))
    );

    private static final Map<Class<?>, ?> defaultValuesRegistry = Map.ofEntries(
            Map.entry(boolean.class, false),
            Map.entry(byte.class, 0),
            Map.entry(short.class, 0),
            Map.entry(int.class, 0),
            Map.entry(long.class, 0L),
            Map.entry(char.class, '\u0000'),
            Map.entry(float.class, 0.0f),
            Map.entry(double.class, 0.0d)
    );

    public static Object getDefaultValueForType(final Class<?> clazz) {
        if (isNull(clazz)) {
            throw new ValidationException("Cannot process null source type");
        }

        return Optional.ofNullable(defaultValuesRegistry.get(clazz))
                .orElse(null);
    }

    public static <T> T convert(final String data, final Class<T> clazz) {
        if (isNull(data) || data.isEmpty()) {
            throw new ValidationException("Cannot convert empty or null source");
        }

        final BiFunction<String, Class<?>, ?> convertFunction = Enum.class.isAssignableFrom(clazz)
                ? convertRegistry.get(Enum.class)
                : convertRegistry.get(clazz);

        if (isNull(convertFunction)) {
            throw new UnknownFormatConversionException("Unable to find convert function for type " + clazz.toString());
        }

        return cast(convertFunction.apply(data, clazz), clazz);
    }

    public static <T> T cast(final Object data, final Class<T> clazz) {
        if (isNull(data)) {
            throw new ValidationException("Cannot cast empty or null source");
        }

        try {
            return (T) data;
        } catch (ClassCastException e) {
            throw new ClassCastException("Unable to cast `" + data.getClass() + "` to `" + clazz + "`");
        }
    }

    public static <T> T cast(final Object data) {
        if (isNull(data)) {
            throw new ValidationException("Cannot cast empty or null source");
        }

        return (T) data;
    }
}
