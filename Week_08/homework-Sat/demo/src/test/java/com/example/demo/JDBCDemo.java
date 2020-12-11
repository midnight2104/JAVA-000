package com.example.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PreparedStatement预编译能减少相同SQL的解析时间，需要显示开启。
 */
public class JDBCDemo {
    @Test
    public void jdbcDemo() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test02", "root", "123456");
        connection.setAutoCommit(false);

        //statement 插入100万条记录，耗时：178秒
        //insertDataByStatement(connection);

        //PreparedStatement 插入100万条记录，耗时：200秒
        //insertDataByPreparedStatement(connection);

        //批量插入100万条记录，耗时：10秒
        //insertDataByBatch(connection);

        insertDataByThreadPool(connection);

        connection.commit();
        connection.close();
    }

    private void insertDataByStatement(Connection connection) throws SQLException {
        long start = System.currentTimeMillis();
        Statement statement = connection.createStatement();
        for (int i = 0; i < 100_0000; i++) {
            String sql = "INSERT INTO t_order(order_status, payment_method, order_money, discount_money, express_money, payment_money,address_id, express_id) " +
                    "VALUES(2, 2, 122, 2, 6, 100, 100, 111)";
            statement.execute(sql);
        }
        long end = System.currentTimeMillis();

        System.out.println("statement 插入100万条记录，耗时："+(end-start)/1000 + "秒");
    }

    private void insertDataByPreparedStatement(Connection connection) throws SQLException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100_0000; i++) {
            String sql = "INSERT INTO t_order(order_status, payment_method, order_money, discount_money, express_money, payment_money,address_id, express_id) " +
                    "VALUES(2, 2, 122, 2, 6, 100, 100, 111)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        }
        long end = System.currentTimeMillis();

        System.out.println("PreparedStatement 插入100万条记录，耗时："+(end-start)/1000 + "秒");
    }

    private void insertDataByBatch(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        long start = System.currentTimeMillis();
        for(int k = 0; k < 100; k++){
            for(int j = 0; j < 100; j++){
                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO t_order(order_status, payment_method, order_money, discount_money, express_money, payment_money,address_id, express_id) VALUES" );

                for (int i = 0; i < 100; i++) {
                    sql.append("(2, 2, 122, 2, 6, 100, 100, 111),")                            ;
                }

                statement.addBatch(sql.toString().substring(0,sql.lastIndexOf(",")));
            }
            statement.executeBatch();
        }

        long end = System.currentTimeMillis();

        System.out.println("批量插入100万条记录，耗时："+(end-start)/1000 + "秒");
    }

    private void insertDataByThreadPool(Connection connection) throws SQLException {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long start = System.currentTimeMillis();

        executorService.execute(() -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            for(int k = 0; k < 25; k++){
                for(int j = 0; j < 100; j++){
                    StringBuilder sql = new StringBuilder();
                    sql.append("INSERT INTO t_order(order_status, payment_method, order_money, discount_money, express_money, payment_money,address_id, express_id) VALUES" );

                    for (int i = 0; i < 100; i++) {
                        sql.append("(2, 2, 122, 2, 6, 100, 100, 111),")                            ;
                    }

                    try {
                        statement.addBatch(sql.toString().substring(0,sql.lastIndexOf(",")));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    statement.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });


        long end = System.currentTimeMillis();

        System.out.println("批量插入100万条记录，耗时："+(end-start)/1000 + "秒");

       // executorService.shutdown();
    }

}
