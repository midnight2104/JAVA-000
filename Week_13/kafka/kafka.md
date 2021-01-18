（必做）搭建一个3节点Kafka集群，测试功能和性能；实现spring kafka下对kafka集群
的操作，将代码提交到github。

#### 单机`Kafka`

1. `KafKa`依赖`ZooKeeper`，需要提前安装好它。

2. 官网下载地址[kafka_2.13-2.7.0](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.7.0/kafka_2.13-2.7.0.tgz)

   ```tar -zxvf kafka_2.13-2.7.0.gz```

3. 启动`kafka`

   > 在启动`kafka`之前，需要提前启动`zookeeper`，在`config/server.properties`配置`zookeepe`r的连接地址`zookeeper.connect=192.168.14.130:2181`。
   >
   > 启动`kafka的命令是`bin/kafka-server-start.sh config/server.properties`

4. 创建`topic`

```sh
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
Created topic test.
```

5. 查看`topic`

   ```sh
   bin/kafka-topics.sh --list --bootstrap-server localhost:9092
   
   ```

6. 使用`producer`发送消息

   ```sh
    bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
   >test hello world
   ```

7. 使用`consumer`接受消息

   ```sh
    bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
   test hello world
   ```

在消费者能够看到生产者的消息，那么单体的`kafka`就完成了。

#### Kafka集群

1. 部署一个3节点的`kafka`集群，需要准备好3个配置文件`kafka900x.properties`，分别修改对应的位置。

```sh
broker.id=1 #三个配置文件分别修改为1,2,3
log.dirs=/tmp/kafka-logs1 #三个配置文件分别修改为logs1,logs2,logs3
listeners=PLAINTEXT://localhost:9001 #三个配置文件分别修改为9001,9002,9003
```

2. 清理zk的数据，可以使用工具`ZooInspector`

   ```sh
   D:\zk\ZooInspector\build>java -jar zookeeper-dev-ZooInspector.jar
   ```

3. 启动三个`kafka`

```sh
bin/kafka-server-start.sh kafka9001.properties
bin/kafka-server-start.sh kafka9002.properties
bin/kafka-server-start.sh kafka9003.properties
```

4. 集群测试

   ```sh
   # 创建带有3个分区2连个副本的topic
   bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic test32 --partitions 3 --replication-factor 2
   # 生产者
   bin/kafka-console-producer.sh --bootstrap-server localhost:9003 --topic test32
   #消费者
   bin/kafka-console-consumer.sh --bootstrap-server localhost:9001 --topic test32 --from-beginning
   ```

5. 性能测试

   ```sh
   #producer
   bin/kafka-producer-perf-test.sh --topic test32 --num-records 100000 --record-size 1000 --throughput 2000 --producer-props bootstrap.servers=localhost:9002
   #consumer
   bin/kafka-consumer-perf-test.sh --bootstrap-server localhost:9002 --topic test32 --fetch-size 1048576 --messages 10000 --threads 1
   ```

#### SpringBoot集成Kafka

1. 添加依赖

   ```xml
   	<dependency>
           <groupId>org.springframework.kafka</groupId>
           <artifactId>spring-kafka</artifactId>
   	</dependency>
   ```

2. 属性配置

   ```yaml
   spring:
     kafka:
       #kafka集群
       bootstrap-servers: 192.168.14.130:9001,192.168.14.130:9002,192.168.14.130:9003
       producer:
         key-serializer: org.apache.kafka.common.serialization.StringSerializer
         value-serializer: org.apache.kafka.common.serialization.StringSerializer
         #自定义的topic
         myTopic1: testTopic1
         myTopic2: testTopic2
       consumer:
         group-id: default-group   #默认组id  后面会配置多个消费者组
         key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
         value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
         auto-offset-reset: latest
         enable-auto-commit: false   #关闭自动提交 改由spring-kafka提交
         auto-commit-interval: 100   #自动提交间隔100ms
         max-poll-records: 20      #批量消费 一次接收的最大数量
   ```

3. `Kafka`配置类

   ```java
   @Data
   @Configuration
   public class KafkaConfiguration {
       /**
        * kafaka集群列表
        */
       @Value("${spring.kafka.bootstrap-servers}")
       private String bootstrapServers;
    
       /**
        * kafaka消费group列表
        */
       @Value("${spring.kafka.consumer.group-id}")
       private String defaultGroupId;
       
       /**
        * 消费开始位置
        */
       @Value("${spring.kafka.consumer.auto-offset-reset}")
       private String autoOffsetReset;
    
       /**
        * 是否自动提交
        */
       @Value("${spring.kafka.consumer.enable-auto-commit}")
       private String enableAutoCommit;
    
       /**
        * #如果'enable.auto.commit'为true，则消费者偏移自动提交给Kafka的频率（以毫秒为单位），默认值为5000。
        */
       @Value("${spring.kafka.consumer.auto-commit-interval}")
       private String autoCommitInterval;
    
       /**
        * 一次调用poll()操作时返回的最大记录数，默认值为500
        */
       @Value("${spring.kafka.consumer.max-poll-records}")
       private String maxPollRecords;
   
       /**
        * 自定义的topic1
        */
       @Value("${spring.kafka.producer.myTopic1}")
       private String myTopic1;
   
       /**
        * 自定义的topic2
        */
       @Value("${spring.kafka.producer.myTopic2}")
       private String myTopic2;
   }
   ```

4. `Kafak`的`Producer`

   ```java
   @RestController
   @RequestMapping("/kafka")
   public class KafkaController {
       @Autowired
       private KafkaConfiguration kafkaConfiguration;
       @Autowired
       private KafkaTemplate<String, String> kafkaTemplate;
   
       /**
        * 发送文本消息
        *
        * @param msg
        * @return
        */
       @GetMapping("/send")
       public String send(@RequestParam String msg) {
           ListenableFuture<SendResult<String, String>> resultListenableFuture = kafkaTemplate.send(kafkaConfiguration.getMyTopic1(), msg);
           resultListenableFuture.addCallback(
                   successCallback -> System.out.println("发送成功：topic= " + kafkaConfiguration.getMyTopic1() + " value= " + msg),
                   failureCallback -> System.out.println("发送失败：topic= " + kafkaConfiguration.getMyTopic1() + " value= " + msg));
   
           return "生产者发送消息给topic1：" + msg;
       }
   }
   ```

5. `Kafak`的`Consumer`

   ```java
   @Component
   public class ConsumerListener1 {
       //消费 myTopic1
       @KafkaListener(topics = "${spring.kafka.producer.myTopic1}")
       public void listen(ConsumerRecord<?,String> record) {
           System.out.println(record);
           String value = record.value();
           System.out.println("消费者1接收到消息：" + value);
       }
   }
   ```

   

参考文献：

- [Kafka集群搭建](https://segmentfault.com/a/1190000023379555)
- [How to Set JAVA_HOME Path in Ubuntu 18.04 and 20.04 LTS](https://vitux.com/how-to-setup-java_home-path-in-ubuntu/)
- [ZooInspector使用](https://www.cnblogs.com/lwcode6/p/11586537.html)