package org.elpis.reactive.websockets.util;

@FunctionalInterface
public interface TriFunction<A,B,C,R> {
    R apply(final A a, final B b, final C c);
}