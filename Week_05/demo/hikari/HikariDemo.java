package com.example.demo.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 配置 Hikari 连接池，改进上述操作。
 */
public class HikariDemo {
    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
        config.setUsername("root");
        config.setPassword("123456");
        config.setMinimumIdle(10);
        config.setMaximumPoolSize(10);
        config.setPoolName("pool");

        HikariPool pool = new HikariPool(config);

        // 查询
        String query = "select * from user";
        PreparedStatement queryStatement = pool.getConnection().prepareStatement(query);
        ResultSet resultSet = queryStatement.executeQuery();
        while (resultSet.next()) {
            String id = resultSet.getString("USER_ID");
            String name = resultSet.getString("USER_NAME");
            System.out.println("USER_ID:" + id + ",USER_NAME:" + name);
        }

        // 新增一条记录
        String insert = "insert into user (USER_ID, USER_NAME) VALUES ('2', 'tom')";
        PreparedStatement insertStatement = pool.getConnection().prepareStatement(insert);
        insertStatement.execute();

        // 修改一条数据
        String update = "update user set USER_NAME = 'tom123' where USER_ID = '2'";
        PreparedStatement updateStatement = pool.getConnection().prepareStatement(update);
        updateStatement.execute();

        // 删除数据
        String delete = "delete from user where USER_NAME = 'tom'";
        PreparedStatement deleteStatement = pool.getConnection().prepareStatement(delete);
        deleteStatement.executeUpdate();

        pool.shutdown();
    }
}