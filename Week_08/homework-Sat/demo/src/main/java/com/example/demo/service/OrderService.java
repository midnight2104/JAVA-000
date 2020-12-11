package com.example.demo.service;



import com.example.demo.domain.Order;
import com.example.demo.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;


    @Transactional(rollbackFor = Exception.class)
    public void insert(Order order) {
        orderMapper.insert(order);
    }

    public List<Order> selectById(Integer id) {
        return orderMapper.selectById(id);
    }



}
