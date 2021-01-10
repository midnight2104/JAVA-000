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
