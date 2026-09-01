package org.spongepowered.asm.mixin.injection;
import java.lang.annotation.*;
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface At {
    String value();
    String target() default "";
}
