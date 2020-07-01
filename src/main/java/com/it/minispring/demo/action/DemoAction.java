package com.it.minispring.demo.action;

import com.it.minispring.demo.service.IDemoService;
import com.it.minispring.mvcframework.annotation.MyAutoWired;
import com.it.minispring.mvcframework.annotation.MyController;
import com.it.minispring.mvcframework.annotation.MyRequestMapping;
import com.it.minispring.mvcframework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lls
 * @version 1.0
 * @date 2020/6/28 11:25
 */
/**
 * 使用自定义注解
 * */
@MyController
public class DemoAction {
    @MyAutoWired
    private IDemoService service;

    @MyRequestMapping("/query.do")
    public void query(HttpServletResponse response, @MyRequestParam("name") String name){
        //String result = service.query(name);
        try {
            //response.getWriter().write(result);
            System.out.println("name="+name);
            response.getWriter().write(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
