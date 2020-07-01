package com.it.minispring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author lls
 * @version 1.0
 * @date 2020/6/28 0:23
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    String value() default "";
}
