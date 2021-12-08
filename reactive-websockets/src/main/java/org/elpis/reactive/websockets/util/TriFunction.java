package org.elpis.reactive.websockets.util;

import lombok.NonNull;

import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<A,B,C,R> {

    R apply(final A a, final B b, final C c);

    default <V> TriFunction<A, B, C, V> andThen(@NonNull final Function<? super R, ? extends V> after) {
        return (A a, B b, C c) -> after.apply(apply(a, b, c));
    }
}