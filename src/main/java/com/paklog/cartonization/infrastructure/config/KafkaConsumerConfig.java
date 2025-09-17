package com.paklog.cartonization.infrastructure.config;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.consumer.group-id}")
    private String groupId;

    @Value("${app.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${app.kafka.consumer.enable-auto-commit:false}")
    private boolean enableAutoCommit;

    @Value("${app.kafka.consumer.session-timeout-ms:30000}")
    private int sessionTimeoutMs;

    @Value("${app.kafka.consumer.heartbeat-interval-ms:10000}")
    private int heartbeatIntervalMs;

    @Value("${app.kafka.consumer.max-poll-records:500}")
    private int maxPollRecords;

    @Value("${app.kafka.consumer.max-poll-interval-ms:300000}")
    private int maxPollIntervalMs;

    @Value("${app.kafka.consumer.concurrency:3}")
    private int concurrency;


    @Bean
    public ConsumerFactory<String, CloudEvent> cloudEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);
        
        // Offset management
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        
        // Session and heartbeat configuration
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);
        
        // Polling configuration
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        
        // Performance tuning
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        // Connection configuration
        configProps.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000);
        configProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);

        log.info("CloudEvent consumer factory configured");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CloudEvent> cloudEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(cloudEventConsumerFactory());
        factory.setConcurrency(concurrency);
        
        // Configure acknowledgment mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Configure error handling
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
            (record, exception) -> {
                log.error("Error processing CloudEvent: topic={}, partition={}, offset={}, key={}, error={}", 
                         record.topic(), record.partition(), record.offset(), record.key(), exception.getMessage(), exception);
            }
        ));
        
        // Configure container properties
        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setPollTimeout(3000);
        containerProps.setIdleBetweenPolls(1000);
        containerProps.setShutdownTimeout(30000);
        
        log.info("CloudEvent Kafka listener container factory configured with concurrency: {}", concurrency);
        
        return factory;
    }
}