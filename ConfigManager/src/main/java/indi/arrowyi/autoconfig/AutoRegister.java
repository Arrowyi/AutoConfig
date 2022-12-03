package indi.arrowyi.autoconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoRegister {
    enum Type
    {
        BOOLEAN,
        INT,
        FLOAT,
        DOUBLE,
        STRING,
        LONG
    }

    Type type();
    String defaultValue() default "";
    String accessor() default "";

    String defaultLoader() default "";
}
