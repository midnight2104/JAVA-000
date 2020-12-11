package com.example.demo;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

//@Configuration
public class RedisCacheConfig extends CachingConfigurerSupport {

    public KeyGenerator keyGenerator() {

        return (target, method, params) -> {
            String className = target.getClass().getName();
            String methodName = method.getName();
            System.out.println("参数:" + Arrays.toString(params));
            return className + "_" + methodName + "_" + params[0].toString();
        };
    }

}
