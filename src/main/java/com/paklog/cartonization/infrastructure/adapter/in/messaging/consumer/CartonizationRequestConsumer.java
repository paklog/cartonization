package com.paklog.cartonization.infrastructure.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.application.port.out.EventPublisher;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationRequestEvent;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationResponseEvent;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.mapper.CartonizationEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CartonizationRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(CartonizationRequestConsumer.class);

    private final PackingSolutionUseCase packingSolutionUseCase;
    private final EventPublisher eventPublisher;
    private final CartonizationEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    public CartonizationRequestConsumer(PackingSolutionUseCase packingSolutionUseCase,
                                      @Qualifier("kafkaEventPublisher") EventPublisher eventPublisher,
                                      CartonizationEventMapper eventMapper,
                                      ObjectMapper objectMapper) {
        this.packingSolutionUseCase = packingSolutionUseCase;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.cartonization-requests}",
        groupId = "${app.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCartonizationRequest(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP, required = false) Long timestamp,
            Acknowledgment acknowledgment) {

        Instant startTime = Instant.now();
        CartonizationRequestEvent requestEvent = null;

        try {
            log.info("Received cartonization request from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.debug("Payload: {}", payload);

            // Parse the incoming event
            requestEvent = objectMapper.readValue(payload, CartonizationRequestEvent.class);
            
            log.info("Processing cartonization request: {} for order: {}", 
                    requestEvent.getRequestId(), requestEvent.getOrderId());

            // Validate the request
            validateRequest(requestEvent);

            // Convert to command
            CalculatePackingSolutionCommand command = eventMapper.toCommand(requestEvent);

            // Process the packing solution
            PackingSolution solution = packingSolutionUseCase.calculate(command);

            // Calculate processing time
            long processingTimeMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            // Create success response
            CartonizationResponseEvent responseEvent = eventMapper.toSuccessResponse(
                requestEvent, solution, processingTimeMs);

            // Publish response
            publishResponse(responseEvent);

            // Acknowledge the message
            acknowledgment.acknowledge();

            log.info("Successfully processed cartonization request: {} in {} ms", 
                    requestEvent.getRequestId(), processingTimeMs);

        } catch (Exception e) {
            long processingTimeMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            
            log.error("Failed to process cartonization request. Topic: {}, Partition: {}, Offset: {}, Error: {}", 
                     topic, partition, offset, e.getMessage(), e);

            // Handle error response
            handleErrorResponse(requestEvent, e, processingTimeMs);

            // Acknowledge the message even on error to avoid reprocessing
            // In production, you might want to send to a DLQ instead
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
        topics = "${app.kafka.topics.carton-management-requests}",
        groupId = "${app.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCartonManagementRequest(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received carton management request from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.debug("Payload: {}", payload);

            // TODO: Implement carton management request handling
            // This could include operations like:
            // - Create new carton types
            // - Update existing cartons
            // - Deactivate cartons
            // - Bulk carton operations

            acknowledgment.acknowledge();

            log.info("Successfully processed carton management request");

        } catch (Exception e) {
            log.error("Failed to process carton management request. Topic: {}, Partition: {}, Offset: {}, Error: {}", 
                     topic, partition, offset, e.getMessage(), e);

            acknowledgment.acknowledge();
        }
    }

    private void validateRequest(CartonizationRequestEvent request) {
        if (request == null) {
            throw new IllegalArgumentException("Cartonization request cannot be null");
        }

        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required for cartonization");
        }

        // Validate each item
        for (CartonizationRequestEvent.ItemRequest item : request.getItems()) {
            if (item.getSku() == null || item.getSku().trim().isEmpty()) {
                throw new IllegalArgumentException("Item SKU is required");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive");
            }
        }

        log.debug("Cartonization request validation passed for: {}", request.getRequestId());
    }

    private void publishResponse(CartonizationResponseEvent responseEvent) {
        try {
            eventPublisher.publish("cartonization-responses", responseEvent);
            log.debug("Published cartonization response for request: {}", responseEvent.getRequestId());
        } catch (Exception e) {
            log.error("Failed to publish cartonization response for request: {}", 
                     responseEvent.getRequestId(), e);
            // Don't re-throw to avoid reprocessing the original message
        }
    }

    private void handleErrorResponse(CartonizationRequestEvent requestEvent, Exception error, long processingTimeMs) {
        try {
            if (requestEvent != null) {
                CartonizationResponseEvent errorResponse = eventMapper.toErrorResponse(
                    requestEvent, error.getMessage(), processingTimeMs);
                publishResponse(errorResponse);
            }
        } catch (Exception e) {
            log.error("Failed to handle error response", e);
        }
    }
}