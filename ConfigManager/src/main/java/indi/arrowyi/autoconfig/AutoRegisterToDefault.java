package indi.arrowyi.autoconfig;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoRegisterToDefault {

    AutoRegister.Type type();
    String defaultValue() default "";
    String accessor() default "DEFAULT";
    String defaultLoader() default "DEFAULT";
}
