package com.example.demo.jdbc;

import java.sql.*;

/**
 * 使用 JDBC 原生接口，实现数据库的增删改查操作
 */
public class JDBCDemo {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "123456");

        // 查询
        String query = "select * from user";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            String id = resultSet.getString("USER_ID");
            String name = resultSet.getString("USER_NAME");
            System.out.println("USER_ID:" + id + ",USER_NAME:" + name);
        }

        // 新增
        String insert = "insert into user (USER_ID, USER_NAME) VALUES ('2', 'tom')";
        Statement insertStatement = connection.createStatement();
        insertStatement.execute(insert);

        // 修改
        String update = "update user set USER_NAME = 'tom123' where USER_ID = '2'";
        Statement updateStatement = connection.createStatement();
        updateStatement.execute(update);

        // 删除
        String delete = "delete from user where USER_NAME = 'tom'";
        Statement deleteStatement = connection.createStatement();
        deleteStatement.executeUpdate(delete);

        connection.close();
    }
}
