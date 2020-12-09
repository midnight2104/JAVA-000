package com.example.xademo.mapper;

import com.example.xademo.domain.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    List<Order> selectById(Integer id);
}
