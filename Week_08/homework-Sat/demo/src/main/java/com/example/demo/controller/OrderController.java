package com.example.demo.controller;


import com.example.demo.domain.Order;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;



    @GetMapping("/test")
    public String test(){
        Order order2 = Order.builder().id(6).orderName("666 666").build();
       orderService.insert(order2);

       return "success";
    }


}
