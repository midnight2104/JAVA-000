学习笔记

1. 周四作业：（必做）设计对前面的订单表数据进行水平分库分表，拆分2个库，每个库16张表。
并在新结构在演示常见的增删改查操作。代码、`sql `和配置文件，上传到 `Github`。
代码在`homework-Thur`。使用`apache-shardingsphere-5.0.0-alpha-shardingsphere-proxy-bin`完成分库分表的任务。测试时启动`shardingsphere-proxy`，默认3307端口。当作一个数据库进行登录，插入数据时，根据分片规则，将数据插入到不同的库，表。

2. （必做）基于hmily TCC或ShardingSphere的Atomikos XA实现一个简单的分布式 事务应用demo（二选一），提交到github。

   代码在`homework-Sat\xademo\`，`homework-Sat\demo\`工程是用于事务2的插入。XA事务的实现使用`shardingsphere-5.0.0-alph`来做，一直没有做成功，事务1会回滚，事务2不会回滚。测试代码在`com.example.xademo.service.OrderService.xaTest1()`。老师能看看是什么原因吗？谢谢~

   ```java
       /**
        * 模拟分布式事务
        * 测试结果失败了：事务1会回滚，事务2不会回滚。
        * 问题： shardingsphere 能够解决这种事务吗？
        */xiexie
       @Transactional(rollbackFor = Exception.class)
       @ShardingTransactionType(TransactionType.XA)
       public void xaTest1() {
           //事务1
           Order order1 = Order.builder().id(6).orderName("666").build();
           orderMapper.insert(order1);
   
           //事务2
           //调用另一个事务 http://localhost:8080/test ，一个插入操作
           String url = "http://localhost:8080/test";
           ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
           System.out.println(forEntity);
   
           //模拟异常，事务1会回滚，事务2不会回滚
           System.out.println(1 / 0);
    }
   ```
   
   