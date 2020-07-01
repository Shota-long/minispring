package com.it.minispring.demo.service.impl;

import com.it.minispring.demo.service.IDemoService;
import com.it.minispring.mvcframework.annotation.MyService;

/**
 * @author lls
 * @version 1.0
 * @date 2020/6/28 11:27
 */
@MyService("testService")
public class DemoServie implements IDemoService {
    @Override
    public String query(String name) {
        return "My Name is"+name;
    }
}
