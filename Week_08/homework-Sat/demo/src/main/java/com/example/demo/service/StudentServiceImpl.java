package com.example.demo.service;

import com.example.demo.domain.Student;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.service.ISomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class StudentServiceImpl implements ISomeService {

    @Autowired
    private StudentMapper mapper;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @CacheEvict(value = "realTimeCache", allEntries = true)
    @Transactional
    public void addStudent(Student student) {
      //  mapper.insertStudent(student);
      //  System.out.println(1 / 0);
        mapper.insertStudent(student);

    }

    @Cacheable(value = "realTimeCache")
    @Override
    public Student findById(Integer id) {
        System.out.println("从DB中查询student");
        return mapper.selectById(id);
    }

    //使用双重检测锁解决热点缓存失效的问题
    @Override
    public Integer countStu() {
       BoundValueOperations<Object,Object>   ops = redisTemplate.boundValueOps("count");

       Object count = ops.get();
       if(count == null){
           synchronized (this){
               count = ops.get();
               if(count==null){
                   count = mapper.countStu();
                   //将数据放到缓存中，并设置过期时间
                   ops.set(count,10, TimeUnit.SECONDS);
               }
           }
       }
        return (Integer) count;
    }
}
