package com.example.demo.service;

import com.example.demo.domain.Student;

public interface ISomeService {

    void addStudent(Student student);
    Student findById(Integer id);
    Integer countStu();
}
