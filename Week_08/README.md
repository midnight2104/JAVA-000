学习笔记

1. 周四作业：（必做）设计对前面的订单表数据进行水平分库分表，拆分2个库，每个库16张表。
并在新结构在演示常见的增删改查操作。代码、`sql `和配置文件，上传到 `Github`。
使用`apache-shardingsphere-5.0.0-alpha-shardingsphere-proxy-bin`完成分库分表的任务。测试时启动`shardingsphere-proxy`，默认3307端口。当作一个数据库进行登录，插入数据时，根据分片规则，将数据插入到不同的库，表。

2. （必做）基于hmily TCC或ShardingSphere的Atomikos XA实现一个简单的分布式 事务应用demo（二选一），提交到github。

   使用`shardingsphere-5.0.0-alph`来做，还没有做成功。关键代码在`com.example.xademo.service.OrderService`。

   ```java
   /**
        * 模拟分布式事务,5.0.0没有这个@ShardingTransactionType注解了吗
        */
       //@ShardingTransactionType(TransactionType.XA)
       @Transactional(rollbackFor = Exception.class)
       public void xaTest() {
           //事务1
           Order order1 = Order.builder().id(6).orderName("666").build();
           orderMapper.insert(order1);
   
           //事务2
           //调用另一个事务 http://localhost:8080/test，是一个添加操作。
           String url = "http://localhost:8080/test";
           ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
           System.out.println(forEntity);
   
           //模拟异常，事务1会回滚，事务2也要回滚才对
           System.out.println(1 / 0);
       }
   ```

   