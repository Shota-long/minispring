package com.spring.develop.annotation;

import java.lang.annotation.*;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/10 23:20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MyComponent {

    String value() default "";
}
