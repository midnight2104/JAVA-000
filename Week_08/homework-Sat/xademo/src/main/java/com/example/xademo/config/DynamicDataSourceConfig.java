package com.example.xademo.config;

import com.example.xademo.constants.DataSourceConstant;
import com.example.xademo.context.DynamicDataSourceContextHolder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源配置
 **/
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@Configuration
@PropertySource("classpath:jdbc.properties")
public class DynamicDataSourceConfig {
    @Bean(DataSourceConstant.DS1)
    @ConfigurationProperties(prefix = "spring.datasource.ds1")
    public DataSource ds1() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceConstant.DS2)
    @ConfigurationProperties(prefix = "spring.datasource.ds2")
    public DataSource ds2() {
        return DataSourceBuilder.create().build();
    }


    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>(3);
        dataSourceMap.put(DataSourceConstant.DS1, ds1());
        dataSourceMap.put(DataSourceConstant.DS2, ds2());

        //设置动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.setDefaultTargetDataSource(ds1());

        return dynamicDataSource;
    }

}
