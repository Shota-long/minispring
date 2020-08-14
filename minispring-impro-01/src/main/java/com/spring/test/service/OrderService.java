package com.spring.test.service;

import com.spring.develop.annotation.MyAutoWired;
import com.spring.develop.annotation.MyComponent;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/12 18:17
 */
@MyComponent("order_service")
public class OrderService {

    @MyAutoWired
    private UserService userService;

    public void test(){
        userService.test();
    }
}
