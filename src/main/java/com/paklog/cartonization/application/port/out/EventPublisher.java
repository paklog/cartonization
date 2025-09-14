package com.paklog.cartonization.application.port.out;

public interface EventPublisher {
    void publish(String topic, Object event);
    void publish(String topic, String key, Object event);
}