package org.elpis.reactive.socket.utils;

import java.util.List;
import java.util.Optional;

public class ListUtils {
    public static <T> Optional<T> getFirstItem(T[] list) {
        return Optional.ofNullable(list)
                .filter(array -> array.length > 0)
                .map(array -> array[0]);
    }

    public static <T> Optional<T> getFirstItem(List<T> list) {
        return Optional.ofNullable(list)
                .filter(l -> l.size() > 0)
                .flatMap(l -> l.stream().findFirst());
    }
}
