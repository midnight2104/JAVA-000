（必做）搭建ActiveMQ服务，基于JMS，写代码分别实现对于que和topic的消息
生产和消费，代码提交到github。

1. 下载地址：http://activemq.apache.org/download-archives.html ，下载解压后进入win32或win64启动activemq.bat。

2. 访问http://localhost:8161/admin（用户名和密码默认为admin），则启动成功。

3. 创建一个springboot项目，添加依赖

```xml
  <!--ActiveMq-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-activemq</artifactId>
        </dependency>
        <!--消息队列连接池-->
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-pool</artifactId>
        </dependency>
```

4. application.yml配置

```yaml
server:
  port: 8080

spring:
  activemq:
    broker-url: tcp://127.0.0.1:61616
    user: admin
    password: admin
    close-timeout: 15s   # 在考虑结束之前等待的时间
    in-memory: true      # 默认代理URL是否应该在内存中。如果指定了显式代理，则忽略此值。
    non-blocking-redelivery: false  # 是否在回滚回滚消息之前停止消息传递。这意味着当启用此命令时，消息顺序不会被保留。
    send-timeout: 0     # 等待消息发送响应的时间。设置为0等待永远。
    queue-name: active.queue
    topic-name: active.topic.name.model

  #  packages:
  #    trust-all: true #不配置此项，会报错
  pool:
    enabled: true
    max-connections: 10   #连接池最大连接数
    idle-timeout: 30000   #空闲的连接过期时间，默认为30秒

  jms:
    pub-sub-domain: true  #默认情况下activemq提供的是queue模式，若要使用topic模式需要配置下面配置
```

5. 启动类增加 **@EnableJms** 注解

```java
@SpringBootApplication
@EnableJms    //启动消息队列
public class MQDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MQDemoApplication.class, args);
	}

}
```

6. 初始化和配置 ActiveMQ 的连接

   ```java
   package com.example.mqdemo;
   
   import org.apache.activemq.ActiveMQConnectionFactory;
   import org.apache.activemq.command.ActiveMQQueue;
   import org.apache.activemq.command.ActiveMQTopic;
   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.jms.config.JmsListenerContainerFactory;
   import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
   import org.springframework.jms.core.JmsMessagingTemplate;
   
   import javax.jms.ConnectionFactory;
   import javax.jms.Queue;
   import javax.jms.Topic;
   
   @Configuration
   public class ActiveMQConfig {
       @Value("${spring.activemq.broker-url}")
       private String brokerUrl;
   
       @Value("${spring.activemq.user}")
       private String username;
   
       @Value("${spring.activemq.password}")
       private String password;
   
       @Value("${spring.activemq.queue-name}")
       private String queueName;
   
       @Value("${spring.activemq.topic-name}")
       private String topicName;
   
       @Bean(name = "queue")
       public Queue queue() {
           return new ActiveMQQueue(queueName);
       }
   
       @Bean(name = "topic")
       public Topic topic() {
           return new ActiveMQTopic(topicName);
       }
   
       @Bean
       public ConnectionFactory connectionFactory() {
           return new ActiveMQConnectionFactory(username, password, brokerUrl);
       }
   
       @Bean
       public JmsMessagingTemplate jmsMessageTemplate() {
           return new JmsMessagingTemplate(connectionFactory());
       }
   
       // 在Queue模式中，对消息的监听需要对containerFactory进行配置
       @Bean("queueListener")
       public JmsListenerContainerFactory<?> queueJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
           SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
           factory.setConnectionFactory(connectionFactory);
           factory.setPubSubDomain(false);
           return factory;
       }
   
       //在Topic模式中，对消息的监听需要对containerFactory进行配置
       @Bean("topicListener")
       public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
           SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
           factory.setConnectionFactory(connectionFactory);
           factory.setPubSubDomain(true);
           return factory;
       }
   }
   ```

   7. 生产者

      ```java
      package com.example.mqdemo;
      
      import org.springframework.beans.factory.annotation.Autowired;
      import org.springframework.jms.core.JmsMessagingTemplate;
      import org.springframework.web.bind.annotation.RequestMapping;
      import org.springframework.web.bind.annotation.RequestParam;
      import org.springframework.web.bind.annotation.RestController;
      
      import javax.jms.Queue;
      import javax.jms.Topic;
      
      /**
       * 生产者
       */
      @RestController
      @RequestMapping("/produce")
      public class ProducerController {
      
          @Autowired
          private JmsMessagingTemplate jmsMessagingTemplate;
      
          @Autowired
          private Queue queue;
      
          @Autowired
          private Topic topic;
      
          /**
           * 向queue发送消息
           *
           * @param msg 消息
           */
          @RequestMapping("/queue")
          public String sendByQueue(@RequestParam String msg) {
              this.jmsMessagingTemplate.convertAndSend(this.queue, msg);
              return "success";
          }
      
          /**
           * 向topic发送消息
           *
           * @param msg 消息
           */
          @RequestMapping("/topic")
          public String sendByTopic(@RequestParam String msg) {
              this.jmsMessagingTemplate.convertAndSend(this.topic, msg);
              return "success";
          }
      }
      
      ```

      8. 消费者

         基于PTP模式的消费者

         ```java
         package com.example.mqdemo;
         
         import org.springframework.jms.annotation.JmsListener;
         import org.springframework.stereotype.Component;
         
         /**
          * PTP模式的消费者
          */
         @Component
         public class PTPConsumer {
         
             /**
              * 监听并读取消息
              * @param msg 消息
              */
             @JmsListener(destination="${spring.activemq.queue-name}", containerFactory="queueListener")
             public void queue(String msg) {
                 System.out.println("正在消费：" + msg);
             }
         }
         
         ```

         基于PubSub模式的消费者

         ```java
         
         	package com.example.mqdemo;
         
         import org.springframework.jms.annotation.JmsListener;
         import org.springframework.stereotype.Component;
         
         /**
          * 发布订阅模式的消费者
          */
         @Component
         public class PubSubConsumer {
         
             /**
              * 监听并读取消息
              *
              * @param msg 消息
              */
             @JmsListener(destination = "${spring.activemq.topic-name}", containerFactory = "topicListener")
             public void topic1(String msg) {
                 System.out.println("消费者1 正在消费：" + msg);
             }
         
             @JmsListener(destination = "${spring.activemq.topic-name}", containerFactory = "topicListener")
             public void topic2(String msg) {
                 System.out.println("消费者2 正在消费：" + msg);
             }
         
         
             @JmsListener(destination = "${spring.activemq.topic-name}", containerFactory = "topicListener")
             public void topic3(String msg) {
                 System.out.println("消费者3 正在消费：" + msg);
             }
         }
         ```

         