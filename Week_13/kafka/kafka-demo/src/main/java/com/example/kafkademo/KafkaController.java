package com.example.kafkademo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        System.out.println("==========>>>>>>>>>>>>>"+msg);
        ListenableFuture<SendResult<String, String>> resultListenableFuture = kafkaTemplate.send(kafkaConfiguration.getMyTopic1(), msg);
        resultListenableFuture.addCallback(
                successCallback -> System.out.println("发送成功：topic= " + kafkaConfiguration.getMyTopic1() + " value= " + msg),
                failureCallback -> System.out.println("发送失败：topic= " + kafkaConfiguration.getMyTopic1() + " value= " + msg));

        return "生产者发送消息给topic1：" + msg;
    }
}