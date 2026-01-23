package io.github.elpis.reactive.websockets.flowcontrol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeFlow {
  Class<? extends FlowControlPolicy> value();
}
