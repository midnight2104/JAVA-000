package com.example.xademo.controller;

import com.example.xademo.domain.Order;
import com.example.xademo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/get/{id}")
    public List<Order> findById(@PathVariable Integer id){
        return orderService.selectById(id);
    }

    @PostMapping("/add")
    public void add(@RequestBody Order order) {

         orderService.insert(order);
    }

    @GetMapping("/xa")
    public String xa() {
       // orderService.txTest();
       orderService.xaTest();
        return "success";
    }

}
