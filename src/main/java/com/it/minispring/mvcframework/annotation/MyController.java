package com.it.minispring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author lls
 * @version 1.0
 * @date 2020/6/28 0:21
 */
//元注解@Target只作用在类或接口上
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    //设置注解中valued的默认值为空串"";
    String value() default "";
}
