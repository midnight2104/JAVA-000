package com.example.demo.controller;


import com.example.demo.domain.Student;
import com.example.demo.service.ISomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private ISomeService service;

    @GetMapping("/add")
    public void insert(){
        Student stu = new Student();
        stu.setUsername("Tom");
        stu.setAge(22);

        System.out.println("add student");
        service.addStudent(stu);
    }

    @GetMapping("/get/{id}")
    public Student findById(@PathVariable Integer id){
        return service.findById(id);
    }

    @GetMapping("/count")
    public Integer countStu(){
        return service.countStu();
    }

}
