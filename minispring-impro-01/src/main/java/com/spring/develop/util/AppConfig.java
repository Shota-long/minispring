package com.spring.develop.util;

import com.spring.develop.annotation.MyComponent;
import com.spring.develop.annotation.MyComponentScan;
import com.spring.develop.annotation.MyConfiguration;

/**
 * @author lls
 * @version 1.0
 * @date 2020/8/10 23:10
 */
@MyComponentScan("com.spring.test")
@MyConfiguration
public class AppConfig {
}
