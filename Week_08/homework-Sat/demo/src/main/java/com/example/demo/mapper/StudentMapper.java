package com.example.demo.mapper;

import com.example.demo.domain.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 有了注解 @Mapper
 * 就不需要再配置 mytabis.mapper-location = classpath:/com/example/demo/mapper/*.xml
 */
@Mapper
public interface StudentMapper {

    void insertStudent(Student student);

    Student selectById(Integer id);
    Integer countStu();
}
