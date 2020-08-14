package com.spring.test;

import com.spring.develop.util.AppConfig;
import com.spring.develop.util.MyApplicationContext;
import com.spring.test.service.OrderService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/10 23:41
 */
public class Test {

    public static void main(String[] args) {

        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);
        OrderService orderService = (OrderService) applicationContext.getBean("orderService");
        orderService.test();
    }
}
