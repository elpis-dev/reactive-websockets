package org.elpis.reactive.socket.web.context.resource.data;

import org.elpis.reactive.websockets.web.annotation.controller.SocketController;

@SocketController("/path")
public class PathVariableSocketResource {

//    @SendMapping("/single/get/string/{id}")
//    public Publisher<?> getWithStringPath(@SocketPathVariable("id") final String id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/no/string/{id}")
//    public Publisher<?> getWithNoStringPath(@SocketPathVariable("ids") final String id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/byte/{id}")
//    public Publisher<?> getWithNumericBytePath(@SocketPathVariable("id") final Byte id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/primitive/byte/{id}")
//    public Publisher<?> getWithNumericPrimitiveBytePath(@SocketPathVariable("id") final byte id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/short/{id}")
//    public Publisher<?> getWithNumericShortPath(@SocketPathVariable("id") final Short id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/primitive/short/{id}")
//    public Publisher<?> getWithNumericShortPrimitivePath(@SocketPathVariable("id") final short id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/int/{id}")
//    public Publisher<?> getWithNumericIntPath(@SocketPathVariable("id") final Integer id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/primitive/int/{id}")
//    public Publisher<?> getWithNumericIntPrimitivePath(@SocketPathVariable("id") final int id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/long/{id}")
//    public Publisher<?> getWithNumericLongPath(@SocketPathVariable("id") final Long id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/numeric/primitive/long/{id}")
//    public Publisher<?> getWithNumericLongPrimitivePath(@SocketPathVariable("id") final long id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/boolean/{id}")
//    public Publisher<?> getWithBooleanPath(@SocketPathVariable("id") final Boolean id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/boolean/primitive/{id}")
//    public Publisher<?> getWithBooleanPrimitivePath(@SocketPathVariable("id") final boolean id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/enum/{id}")
//    public Publisher<?> getEnumPath(@SocketPathVariable("id") final BootStarter.Example id) {
//        return Flux.just(Map.of("path", id));
//    }
//
//    @SendMapping("/single/get/no/required/{id}")
//    public Publisher<?> getWithNoRequiredPath(@SocketPathVariable(value = "no", required = false) final String id) {
//        return Flux.just(Map.of("path", String.valueOf(id)));
//    }

}
