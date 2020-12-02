package com.xd.springbootshardingreadwrite.controller;

import com.xd.springbootshardingreadwrite.entity.User;
import com.xd.springbootshardingreadwrite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/select")
    public List<User> select() {
        return userService.getUserList();
    }

    @PostMapping("/insert")
    public Boolean insert(User user) {
        return userService.save(user);
    }

}
