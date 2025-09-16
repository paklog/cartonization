package com.paklog.cartonization.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.cartonization.application.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getClass().getSimpleName(), eventJson);

            log.info("Published event {} to topic {}", event.getClass().getSimpleName(), topic);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public void publish(String topic, String key, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, eventJson);

            log.info("Published event {} with key {} to topic {}",
                    event.getClass().getSimpleName(), key, topic);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}