package com.spring.develop.util;

import com.spring.develop.annotation.MyComponent;
import com.spring.develop.annotation.MyComponentScan;
import com.spring.develop.annotation.Scope;
import com.spring.develop.entities.BeanDefinition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/10 23:41
 */
public class MyApplicationContext {

    private String scanPath;
    //存放每个对象的bd
    private ConcurrentHashMap<String, BeanDefinition> bdMap = new ConcurrentHashMap<>();
    //单例池
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public MyApplicationContext(Class<?> appConfigClass) {
        //判断启动类中是否含有该扫描注解
        if (appConfigClass.isAnnotationPresent(MyComponentScan.class)){
            MyComponentScan contextPath = (MyComponentScan) appConfigClass.getAnnotation(MyComponentScan.class);
            //获取扫描路径
            scanPath = contextPath.value();
            //System.out.println(contextPath.value());
            doScanPath(scanPath);
        }
        //生成bean
        doInstance(bdMap);
    }

    private void doInstance(ConcurrentHashMap<String, BeanDefinition> bdMap) {
        for (String beanName : bdMap.keySet()) {
            BeanDefinition beanDefinition = bdMap.get(beanName);
            if (beanDefinition.getScope() == "singleton"){
                Object bean = createBean(beanDefinition);
                singletonObjects.put(beanName,bean);
            }
            
        }
    }

    private Object createBean(BeanDefinition beanDefinition){
        Object instance = null;
        try {
            instance = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return  instance;
    }

    //扫描路径中的类
    private void doScanPath(String scanPath) {
        //把“com.spring.test”--》“com/spring/test”
        URL url = this.getClass().getClassLoader().getResource(scanPath.replace(".", "/"));
        //System.out.println(url);
        File file = new File(url.getFile());
        //读取文件内容，文件可能为.class,也可能是文件夹，也可能是其他文件
        File[] files = file.listFiles();
        for (File file1 : files) {
            System.out.println(file1);
            System.out.println(file1.getName());
            if (file1.isDirectory()){
                doScanPath(scanPath+"."+file1.getName());
            }else{
                try {
                    Class<?> aClass = this.getClass().getClassLoader().loadClass(scanPath + "." + file1.getName().replace(".class",""));
                    if (aClass.isAnnotationPresent(MyComponent.class)){
                        String beanName = aClass.getAnnotation(MyComponent.class).value();
                        //beanName使用默认的时候,即为""，则beanName为类名第一个字母小写
                        if (beanName.equals("")){
                            beanName = lowerFirstCase(aClass.getSimpleName());
                        }
                        //每个Bean都有自己独立的bd,bd中不需要设置属性beanName，beanName为bdMap的key
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setBeanClass(aClass);
                        //判断是否存在@Scope
                        if (aClass.isAnnotationPresent(Scope.class)){
                            beanDefinition.setScope(aClass.getAnnotation(Scope.class).value());
                        }
                        else{
                            //不添加Scope注解默认为单例
                            beanDefinition.setScope("singleton");
                        }
                        bdMap.put(beanName,beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //IOC容器中bean的key为类名首字母小写
    private String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        //首字母小写
        chars[0]+=32;
        return String.valueOf(chars);
    }

    public Object getBean(String beanName){
        BeanDefinition beanDefinition = bdMap.get(beanName);
        if (beanDefinition.getScope() == "singleton"){

        }
        return null;
    }

    public Object getBean(Class beanName){
        return null;
    }
}
