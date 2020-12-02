package me.mason.demo.dynamicdatasource.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import me.mason.demo.dynamicdatasource.entity.TestUser;
import me.mason.demo.dynamicdatasource.mapper.TestUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TestUserService {
    @Autowired
    private TestUserMapper testUserMapper;


    /**
     * 查询master库User
     * @return
     */
    public List<TestUser> getMasterUser(){
        QueryWrapper<TestUser> queryWrapper = new QueryWrapper<>();
        return testUserMapper.selectAll(queryWrapper.isNotNull("name"));
    }

    /**
     * 查询slave库User
     * @return
     */
    public List<TestUser> getUser(){
        return testUserMapper.selectList(null);
    }
}
