package com.paklog.cartonization.infrastructure.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeadLetterQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    private final ObjectMapper objectMapper;

    public DeadLetterQueueConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.cartonization-requests-dlq}",
        groupId = "${app.kafka.consumer.group-id}-dlq",
        containerFactory = "cloudEventKafkaListenerContainerFactory"
    )
    public void handleDeadLetterMessage(
            @Payload CloudEvent cloudEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(value = KafkaHeaders.EXCEPTION_STACKTRACE, required = false) String exceptionStacktrace,
            @Header(value = KafkaHeaders.ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(value = KafkaHeaders.ORIGINAL_PARTITION, required = false) Integer originalPartition,
            @Header(value = KafkaHeaders.ORIGINAL_OFFSET, required = false) Long originalOffset,
            Acknowledgment acknowledgment) {

        try {
            log.error("Processing dead letter CloudEvent from topic: {}, partition: {}, offset: {}",
                     topic, partition, offset);
            log.error("CloudEvent details: id={}, type={}, source={}",
                     cloudEvent.getId(), cloudEvent.getType(), cloudEvent.getSource());

            if (originalTopic != null) {
                log.error("Original message was from topic: {}, partition: {}, offset: {}",
                         originalTopic, originalPartition, originalOffset);
            }

            if (exceptionMessage != null) {
                log.error("Exception that caused DLQ: {}", exceptionMessage);
            }

            // Extract payload from CloudEvent
            String payload = extractPayloadFromCloudEvent(cloudEvent);

            // Parse and log the failed message for analysis
            logFailedMessage(payload, exceptionMessage, originalTopic);

            // Store failed message for manual review or automated retry
            storeFailedMessage(payload, exceptionMessage, originalTopic, originalPartition, originalOffset);

            // Could implement retry logic here if appropriate
            // evaluateForRetry(payload, exceptionMessage);

            acknowledgment.acknowledge();

            log.info("Successfully processed dead letter CloudEvent from topic: {}", topic);

        } catch (Exception e) {
            log.error("Failed to process dead letter CloudEvent. Topic: {}, Partition: {}, Offset: {}, Error: {}",
                     topic, partition, offset, e.getMessage(), e);

            // Acknowledge even on error to prevent infinite loop
            acknowledgment.acknowledge();
        }
    }

    private String extractPayloadFromCloudEvent(CloudEvent cloudEvent) {
        try {
            if (cloudEvent.getData() == null) {
                return "{}";
            }
            return new String(cloudEvent.getData().toBytes());
        } catch (Exception e) {
            log.warn("Failed to extract payload from CloudEvent: {}", e.getMessage());
            return "{}";
        }
    }

    private void logFailedMessage(String payload, String exceptionMessage, String originalTopic) {
        try {
            // Attempt to parse as JSON for better logging
            Object parsedPayload = objectMapper.readValue(payload, Object.class);
            log.error("Failed message details: originalTopic={}, exception={}, payload={}", 
                     originalTopic, exceptionMessage, objectMapper.writeValueAsString(parsedPayload));
        } catch (Exception e) {
            // If JSON parsing fails, log as raw string
            log.error("Failed message details: originalTopic={}, exception={}, rawPayload={}", 
                     originalTopic, exceptionMessage, payload);
        }
    }

    private void storeFailedMessage(String payload, String exceptionMessage, String originalTopic, 
                                  Integer originalPartition, Long originalOffset) {
        try {
            // Create a record of the failed message for manual review
            FailedMessageRecord failedRecord = new FailedMessageRecord(
                originalTopic,
                originalPartition,
                originalOffset,
                payload,
                exceptionMessage,
                Instant.now()
            );

            // TODO: Store in database, send to monitoring system, or write to file
            // This could be stored in:
            // 1. A dedicated failed_messages table in MongoDB
            // 2. A separate monitoring system like ELK stack
            // 3. A file for later analysis
            // 4. Sent to alerting system for immediate notification

            log.debug("Stored failed message record: {}", failedRecord);

        } catch (Exception e) {
            log.error("Failed to store failed message record", e);
        }
    }

    private record FailedMessageRecord(
        String originalTopic,
        Integer originalPartition,
        Long originalOffset,
        String payload,
        String exceptionMessage,
        Instant failedAt
    ) {}
}