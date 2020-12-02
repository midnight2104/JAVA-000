package me.mason.demo.dynamicdatasource.aop;

import me.mason.demo.dynamicdatasource.context.DynamicDataSourceContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class DynamicDataSourceAspect {
    private final String[] QUERY_PREFIX = {"select", "find", "get"};

    @Pointcut("execution( * me.mason.demo.dynamicdatasource.mapper.*.*(..))")
    public void daoAspect() {
    }

    @Before("daoAspect()")
    public void switchDataSource(JoinPoint point) {
        Boolean isQueryMethod = isQueryMethod(point.getSignature().getName());
        if (isQueryMethod) {
            DynamicDataSourceContextHolder.useSlaveDataSource();
        }
    }

    @After("daoAspect()")
    public void restoreDataSource(JoinPoint point) {
        DynamicDataSourceContextHolder.clearDataSourceKey();
    }

    private Boolean isQueryMethod(String methodName) {
        for (String prefix : QUERY_PREFIX) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
