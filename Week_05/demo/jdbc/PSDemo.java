package com.example.demo.jdbc;

import java.sql.*;

/**
 * 使用事务，PrepareStatement方式，批处理方式，改进上述操作。
 */
public class PSDemo {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/demo", "root", "123456");
        connection.setAutoCommit(false);

        // 查询
        String query = "select * from user";
        PreparedStatement queryStatement = connection.prepareStatement(query);
        ResultSet resultSet = queryStatement.executeQuery();
        while (resultSet.next()) {
            String id = resultSet.getString("USER_ID");
            String name = resultSet.getString("USER_NAME");
            System.out.println("USER_ID:" + id + ",USER_NAME:" + name);
        }

        // 新增
        String insert = "insert into user (USER_ID, USER_NAME) VALUES ('2', 'tom')";
        PreparedStatement insertStatement = connection.prepareStatement(insert);
        insertStatement.execute();

        // 修改
        String update = "update user set USER_NAME = 'tom123' where USER_ID = '2'";
        PreparedStatement updateStatement = connection.prepareStatement(update);
        updateStatement.execute();

        // 删除
        String delete = "delete from user where USER_NAME = 'tom'";
        PreparedStatement deleteStatement = connection.prepareStatement(delete);
        deleteStatement.executeUpdate();

        connection.commit();
        connection.close();
    }
}
