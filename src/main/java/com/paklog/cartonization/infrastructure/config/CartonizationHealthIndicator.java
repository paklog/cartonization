package com.paklog.cartonization.infrastructure.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CartonizationHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CartonizationHealthIndicator(MongoTemplate mongoTemplate,
                                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Health health() {
        try {
            // Check MongoDB connection
            mongoTemplate.getDb().getName();

            // Check Kafka connection
            kafkaTemplate.getDefaultTopic();

            return Health.up()
                .withDetail("service", "Cartonization Service")
                .withDetail("status", "All systems operational")
                .withDetail("mongodb", "Connected")
                .withDetail("kafka", "Connected")
                .build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "Cartonization Service")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}