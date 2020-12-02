package me.mason.demo.dynamicdatasource.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import me.mason.demo.dynamicdatasource.entity.TestUser;
import me.mason.demo.dynamicdatasource.mapper.TestUserMapper;
import me.mason.demo.dynamicdatasource.service.TestUserService;
import me.mason.demo.dynamicdatasource.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class TestUserController {

    @Autowired
    private TestUserMapper testUserMapper;

    @Autowired
    private TestUserService testUserService;


    /**
     * 查询
     */
    @GetMapping("/find")
    public Object find(int id) {
        TestUser testUser = testUserMapper.selectOne(new QueryWrapper<TestUser>().eq("id", id));
        if (testUser != null) {
            return ResponseResult.success(testUser);
        } else {
            return ResponseResult.error("没有找到该对象");
        }
    }

    /**
     * master数据源负责插操作
     */
    @PostMapping("/insert")
    public Object insert(@RequestBody TestUser testUser) {

        return null;

    }

    /**
     * slave负责 查询
     */
    @GetMapping("/listAll")
    public List<TestUser> listAll() {

        return testUserService.getUser();
    }

}
