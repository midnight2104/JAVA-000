package me.mason.demo.dynamicdatasource.config;

import me.mason.demo.dynamicdatasource.constants.DataSourceConstant;
import me.mason.demo.dynamicdatasource.constants.DataSourceKey;
import me.mason.demo.dynamicdatasource.context.DynamicDataSourceContextHolder;
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
 *
 **/
// 添加此配置，否则 报`The dependencies of some of the beans in the application context form a cycle`
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@Configuration
@PropertySource("classpath:config/jdbc.properties")
@MapperScan(basePackages = "me.mason.demo.dynamicdatasource.mapper")
public class DynamicDataSourceConfig {
    @Bean(DataSourceConstant.DS_KEY_MASTER)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceConstant.DS_KEY_SLAVE)
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(DataSourceConstant.DS_KEY_SLAVE2)
    @ConfigurationProperties(prefix = "spring.datasource.slave2")
    public DataSource slave2DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        Map<Object, Object> dataSourceMap = new HashMap<>(3);
        dataSourceMap.put(DataSourceConstant.DS_KEY_MASTER, masterDataSource());
        dataSourceMap.put(DataSourceConstant.DS_KEY_SLAVE, slaveDataSource());
        dataSourceMap.put(DataSourceConstant.DS_KEY_SLAVE2, slave2DataSource());

        DynamicDataSourceContextHolder.dataSourceKeys.addAll(dataSourceMap.keySet());
        //从库信息
        DynamicDataSourceContextHolder.slaveDataSourceKeys.addAll(dataSourceMap.keySet());
        DynamicDataSourceContextHolder.slaveDataSourceKeys.remove(DataSourceConstant.DS_KEY_MASTER);

        //设置动态数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource());

        return dynamicDataSource;
    }

}
