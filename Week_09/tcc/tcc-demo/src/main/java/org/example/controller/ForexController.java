package org.example.controller;

import org.example.service.ForexServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ForexController {
    @Resource
    private ForexServiceImpl forexService;

    @GetMapping("/tcc")
    public String tcc(){
        forexService.forexHandle();
        return "success";
    }

}
