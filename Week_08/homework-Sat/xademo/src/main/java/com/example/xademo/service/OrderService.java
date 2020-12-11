package com.example.xademo.service;

import com.example.xademo.domain.Order;
import com.example.xademo.mapper.OrderMapper;

import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RestTemplate restTemplate;

    public void insert(Order order) {
        orderMapper.insert(order);
    }

    public List<Order> selectById(Integer id) {
        return orderMapper.selectById(id);
    }

    /**
     * 模拟分布式事务
     */
    @Transactional(rollbackFor = Exception.class)
    public void txTest() {
        //事务1
        Order order1 = Order.builder().id(6).orderName("666").build();
        orderMapper.insert(order1);

        //事务2
        //调用另一个事务 http://localhost:8080/test
        String url = "http://localhost:8080/test";
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        System.out.println(forEntity);

        //模拟异常，事务1会回滚，事务2不会回滚
        System.out.println(1 / 0);
    }

    /**
     * 模拟分布式事务
     * 测试结果失败了：事务1会回滚，事务2不会回滚。
     * 问题： shardingsphere 能够解决这种事务吗？
     */
    @Transactional(rollbackFor = Exception.class)
    @ShardingTransactionType(TransactionType.XA)
    public void xaTest1() {
        //事务1
        Order order1 = Order.builder().id(6).orderName("666").build();
        orderMapper.insert(order1);

        //事务2
        //调用另一个事务 http://localhost:8080/test ，一个插入操作
        String url = "http://localhost:8080/test";
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        System.out.println(forEntity);

        //模拟异常，事务1会回滚，事务2不会回滚
        System.out.println(1 / 0);
    }


    public void xaTest2() {
        //事务1
        Order order1 = Order.builder().id(12).orderName("666 哈哈哈").build();
        orderMapper.insert(order1);

        //事务2
        Order order2 = Order.builder().id(13).orderName("666 哈哈哈").build();
        orderMapper.insert(order1);
    }
}
