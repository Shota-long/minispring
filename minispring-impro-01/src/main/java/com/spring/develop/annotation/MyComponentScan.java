package com.spring.develop.annotation;

import java.lang.annotation.*;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/10 23:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MyComponentScan {

    String value() default "";
}
