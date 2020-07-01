package com.it.minispring.mvcframework.servlet;

import com.it.minispring.mvcframework.annotation.MyAutoWired;
import com.it.minispring.mvcframework.annotation.MyController;
import com.it.minispring.mvcframework.annotation.MyRequestMapping;
import com.it.minispring.mvcframework.annotation.MyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author lls
 * @version 1.0
 * @date 2020/6/28 0:05
 *
 * mvc启动入口
 */

public class LSDispatcherServlet extends HttpServlet {

    //几个成员变量的声明
    private static final long seriaVersionUID = 1L;
    //获取配置文件地址，跟web.xml中param-name值一致
    private static final String LOCATION = "contextConfigLocation";
    //保存所有的配置信息
    private Properties p = new Properties();
    //保存所有扫描类的相关信息
    private List<String> classNames = new ArrayList<String>();
    //IOC容器，保存所有初始化的Bean
    private Map<String,Object> ioc = new HashMap<String, Object>();
    //保存所有的Url和映射关系
    private Map<String, Method> handleMapping = new HashMap<String, Method>();
    //logger
    private static final Logger logger = LoggerFactory.getLogger(LSDispatcherServlet.class);

    public LSDispatcherServlet() {
        super();
    }

    //初始化，加载配置文件
    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("LSDispatcherServlet初始化...");
        //通过config拿到配置信息
        //1、加载配置文件
        doLoadConfig(config.getInitParameter(LOCATION));
        //2、扫描所有的类
        doScanner(p.getProperty("scanPackage"));
        //3、初始化所有相关的实例，并保存到IOC容器中
        doInstance();
        //4、依赖注入
        doAutoWired();
        //5、构造HandlerMapping
        initHandlerMapping();
        
    }

    //将文件读取到properties对象中
    private void doLoadConfig(String initParameter) {
        logger.info("location="+initParameter);
        InputStream in = null;
        //获取该类类路径下的配置文件的流
        try {
            in = this.getClass().getClassLoader().getResourceAsStream(initParameter);
            //in = this.getClass().getClassLoader().getResourceAsStream("application.properties");
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //递归扫描出所有的Class文件
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        //遍历路径下的class文件
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                //递归出完整扫描路径
                doScanner(scanPackage+"."+file.getName());
            }
            else{
                //读取一个class文件，添加到Class集合（com.it.minispring.demo.action.DemoAction）
                classNames.add(scanPackage+"."+file.getName().replace(".class","").trim());
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

    private void doInstance() {
        if (classNames.size()==0){return;}
        try{
            for (String className : classNames) {
                //获取对应类的Class对象
                Class<?> clazz = Class.forName(className);
                //判断class上是否包含有注解，有注解的交给IOC管理
                if (clazz.isAnnotationPresent(MyController.class)){
                    //String beanName = lowerFirstCase(clazz.getSimpleName());
                    //ioc.put(beanName,clazz.getConstructor().newInstance());
                    //获取class上的注解
                    MyController controller = clazz.getAnnotation(MyController.class);
                    //获取class上注解的value值，即自定义的beanName
                    String beanName = controller.value();
                    //如果用户自定义名字，则用该用户自己定义的
                    if(!"".equals(beanName.trim())){
                        ioc.put(beanName.trim(),clazz.getConstructor().newInstance());
                        continue;
                    }else{
                        //如果用户没有自定义beanName
                        beanName = lowerFirstCase(clazz.getSimpleName());
                        ioc.put(beanName.trim(),clazz.getConstructor().newInstance());
                        continue;
                    }
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    //如果用户自定义名字，则用该用户自己定义的
                    if(!"".equals(beanName.trim())){
                        ioc.put(beanName.trim(),clazz.getConstructor().newInstance());
                        continue;
                    }
                    //如果用户没设置，则按接口类型创建一个实例
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        //ioc.put(i.getName(),clazz.getConstructor().newInstance());
                        ioc.put(i.getSimpleName().trim(),clazz.getConstructor().newInstance());
                    }
                }else
                    continue;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doAutoWired() {
        if (ioc.isEmpty()){return;}
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class clazz = entry.getValue().getClass();
            //获取成员变量，包括私有的
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //开启访问私有属性权限
                field.setAccessible(true);
                if (!field.isAnnotationPresent(MyAutoWired.class)){continue;}
                MyAutoWired autoWired = field.getAnnotation(MyAutoWired.class);
                String beanName = autoWired.value().trim();
                //默认beanName
                if ("".equals(beanName)){
                    beanName = lowerFirstCase(field.getType().getSimpleName());
                }
                try {
                    //将指定对象变量上此 Field 对象表示的字段设置为指定的新值。
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
    //将GPRequestMapping中配置的信息和Method进行关联，并保存这些关系。
    private void initHandlerMapping() {
        if (ioc.isEmpty()){return;}
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            //首先它必须含有MyController注解
            if (!clazz.isAnnotationPresent(MyController.class)){continue;}
            String baseUrl = "";
            //获取Controller中url
            //类上的@MyRequestMapping value值
            if (clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            //获取Method中的url配置
            //获取类中所有的方法对象
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)){continue;}
                //映射URL
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                //String url = ("/"+baseUrl+"/"+requestMapping.value().replaceAll("/+","/"));
                String url = baseUrl+requestMapping.value();
                logger.info("url="+url);
                handleMapping.put(url,method);
                System.out.println("mapped:"+url+","+method);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    //执行逻辑业务处理
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            doDispatch(req,resp);
        }catch(Exception e){
            resp.getWriter().write("500 Exception,cause:"+ e.getMessage());
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (this.handleMapping.isEmpty()){return;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        logger.info("requestUrl="+url+",contextPath="+contextPath);
        url = url.replace(contextPath,"").replaceAll("/+","/");
        if (!this.handleMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!");
            return;
        }
        Map<String, String[]> params = req.getParameterMap();
        Method method = this.handleMapping.get(url);
        //获取参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i=0; i<parameterTypes.length; i++){
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class){
                paramValues[i] = req;
                continue;
            }else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            }else if(parameterType == String.class){
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String value = Arrays.toString(entry.getValue())
                                    .replaceAll("\\[|\\]","")
                                    .replaceAll(",\\s",",");
                    paramValues[i] = value;
                }
            }
        }
        try {
            String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
            method.invoke(this.ioc.get(beanName),paramValues);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
