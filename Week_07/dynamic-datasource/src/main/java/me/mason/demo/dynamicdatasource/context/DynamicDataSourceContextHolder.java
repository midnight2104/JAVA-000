package me.mason.demo.dynamicdatasource.context;

import me.mason.demo.dynamicdatasource.constants.DataSourceConstant;
import me.mason.demo.dynamicdatasource.constants.DataSourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 动态数据源名称上下文处理
 *
 **/
public class DynamicDataSourceContextHolder {
    private static Lock lock = new ReentrantLock();
    private static int counter = 0;
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    public static List<Object> dataSourceKeys = new ArrayList<>();
    public static List<Object> slaveDataSourceKeys = new ArrayList<>();

    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }


    public static void useMasterDataSource() {
        CONTEXT_HOLDER.set(DataSourceConstant.DS_KEY_MASTER);
    }

    public static void useSlaveDataSource() {
        lock.lock();
        try {
            int keyIndex = counter % slaveDataSourceKeys.size();
            CONTEXT_HOLDER.set(String.valueOf(slaveDataSourceKeys.get(keyIndex)));
            counter++;
        } catch (Exception e) {
            useMasterDataSource();
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        System.out.println("使用数据源：" + getDataSourceKey());
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }

}
