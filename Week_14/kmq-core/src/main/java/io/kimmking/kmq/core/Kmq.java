package io.kimmking.kmq.core;

import lombok.SneakyThrows;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class Kmq {

    private String topic;

    private int capacity;

    private MyArrayBlockingQueue<KmqMessage> queue;

    public Kmq(String topic, int capacity) {
        this.topic = topic;
        this.capacity = capacity;
        this.queue = new MyArrayBlockingQueue<>(capacity);
    }


    public boolean send(KmqMessage message) {
        return queue.put(message);
    }

    public KmqMessage poll() {
        return queue.take();
    }

//    public Kmq(String topic, int capacity) {
//        this.topic = topic;
//        this.capacity = capacity;
//        this.queue = new LinkedBlockingQueue(capacity);
//    }
//
//    private String topic;
//
//    private int capacity;
//
//    private LinkedBlockingQueue<KmqMessage> queue;
//
//    public boolean send(KmqMessage message) {
//        return queue.offer(message);
//    }
//
//    public KmqMessage poll() {
//        return queue.poll();
//    }
//
//    @SneakyThrows
//    public KmqMessage poll(long timeout) {
//        return queue.poll(timeout, TimeUnit.MILLISECONDS);
//    }

}
