package com.example.xademo.context;


import com.example.xademo.constants.DataSourceConstant;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 动态数据源名称上下文处理
 **/
public class DynamicDataSourceContextHolder {
    private static Lock lock = new ReentrantLock();

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();


    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }


    public static void useDataSource1() {
        CONTEXT_HOLDER.set(DataSourceConstant.DS1);
    }

    public static void useDataSource2() {
        lock.lock();
        try {

            CONTEXT_HOLDER.set(DataSourceConstant.DS2);
        } catch (Exception e) {
            useDataSource1();
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        System.out.println("使用数据源：" + getDataSourceKey());
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

}
