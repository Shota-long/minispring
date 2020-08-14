package com.spring.develop.entities;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/12 23:16
 */
public class BeanDefinition {

    private Class beanClass;
    private String scope;

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
