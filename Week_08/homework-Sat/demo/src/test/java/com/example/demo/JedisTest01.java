package com.example.demo;


import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class JedisTest01 {

    @Test
    public void testJedis() {
        //创建一个Jedis的连接
        Jedis jedis = new Jedis("192.168.14.130", 6379);
        //执行redis命令
        jedis.set("key1", "hello world");
        //从redis中取值
        String result = jedis.get("key1");
        //打印结果
        System.out.println(result);
        //关闭连接
        jedis.close();

    }


}
