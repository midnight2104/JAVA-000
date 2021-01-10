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
