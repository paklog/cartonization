package com.paklog.cartonization.infrastructure.adapter.in.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.cloudevents.CloudEventFactory;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.cloudevents.CloudEventTypes;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationRequestEvent;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationResponseEvent;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.mapper.CartonizationEventMapper;
import com.paklog.cartonization.infrastructure.adapter.out.messaging.CloudEventPublisher;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.JsonFormat;
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
public class CloudEventCartonizationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CloudEventCartonizationConsumer.class);

    private final PackingSolutionUseCase packingSolutionUseCase;
    private final CloudEventPublisher cloudEventPublisher;
    private final CartonizationEventMapper eventMapper;
    private final CloudEventFactory cloudEventFactory;
    private final ObjectMapper objectMapper;

    public CloudEventCartonizationConsumer(PackingSolutionUseCase packingSolutionUseCase,
                                         CloudEventPublisher cloudEventPublisher,
                                         CartonizationEventMapper eventMapper,
                                         CloudEventFactory cloudEventFactory,
                                         ObjectMapper objectMapper) {
        this.packingSolutionUseCase = packingSolutionUseCase;
        this.cloudEventPublisher = cloudEventPublisher;
        this.eventMapper = eventMapper;
        this.cloudEventFactory = cloudEventFactory;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.cartonization-requests}",
        groupId = "${app.kafka.consumer.group-id}",
        containerFactory = "cloudEventKafkaListenerContainerFactory"
    )
    public void handleCartonizationRequestCloudEvent(
            @Payload CloudEvent cloudEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP, required = false) Long timestamp,
            Acknowledgment acknowledgment) {

        Instant startTime = Instant.now();
        CartonizationRequestEvent requestEvent = null;

        try {
            log.info("Received CloudEvent from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("CloudEvent details: id={}, type={}, source={}, subject={}", 
                    cloudEvent.getId(), cloudEvent.getType(), cloudEvent.getSource(), cloudEvent.getSubject());

            // Validate CloudEvent type
            if (!CloudEventTypes.CARTONIZATION_REQUEST.equals(cloudEvent.getType())) {
                log.warn("Unexpected CloudEvent type: {}. Expected: {}", 
                        cloudEvent.getType(), CloudEventTypes.CARTONIZATION_REQUEST);
                acknowledgment.acknowledge();
                return;
            }

            // Extract and parse the event data
            requestEvent = extractCartonizationRequest(cloudEvent);
            
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

            // Publish CloudEvent response
            publishSuccessResponse(cloudEvent, responseEvent);

            // Acknowledge the message
            acknowledgment.acknowledge();

            log.info("Successfully processed cartonization request: {} in {} ms", 
                    requestEvent.getRequestId(), processingTimeMs);

        } catch (Exception e) {
            long processingTimeMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            
            log.error("Failed to process cartonization CloudEvent. Topic: {}, Partition: {}, Offset: {}, Error: {}", 
                     topic, partition, offset, e.getMessage(), e);

            // Handle error response
            handleErrorResponse(cloudEvent, requestEvent, e, processingTimeMs);

            // Acknowledge the message even on error to avoid reprocessing
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
        topics = "${app.kafka.topics.carton-management-requests}",
        groupId = "${app.kafka.consumer.group-id}",
        containerFactory = "cloudEventKafkaListenerContainerFactory"
    )
    public void handleCartonManagementCloudEvent(
            @Payload CloudEvent cloudEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received carton management CloudEvent from topic: {}, partition: {}, offset: {}", 
                    topic, partition, offset);
            log.info("CloudEvent details: id={}, type={}, source={}", 
                    cloudEvent.getId(), cloudEvent.getType(), cloudEvent.getSource());

            // Validate CloudEvent type
            if (!CloudEventTypes.CARTON_MANAGEMENT_REQUEST.equals(cloudEvent.getType())) {
                log.warn("Unexpected CloudEvent type: {}. Expected: {}", 
                        cloudEvent.getType(), CloudEventTypes.CARTON_MANAGEMENT_REQUEST);
                acknowledgment.acknowledge();
                return;
            }

            // TODO: Implement carton management request handling
            // This could include operations like:
            // - Create new carton types
            // - Update existing cartons
            // - Deactivate cartons
            // - Bulk carton operations

            acknowledgment.acknowledge();

            log.info("Successfully processed carton management CloudEvent: {}", cloudEvent.getId());

        } catch (Exception e) {
            log.error("Failed to process carton management CloudEvent. Topic: {}, Partition: {}, Offset: {}, Error: {}", 
                     topic, partition, offset, e.getMessage(), e);

            acknowledgment.acknowledge();
        }
    }

    private CartonizationRequestEvent extractCartonizationRequest(CloudEvent cloudEvent) throws Exception {
        if (cloudEvent.getData() == null) {
            throw new IllegalArgumentException("CloudEvent data is null");
        }

        // Convert CloudEvent data to CartonizationRequestEvent
        byte[] data = cloudEvent.getData().toBytes();
        String jsonData = new String(data);
        
        return objectMapper.readValue(jsonData, CartonizationRequestEvent.class);
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

    private void publishSuccessResponse(CloudEvent originalEvent, CartonizationResponseEvent responseEvent) {
        try {
            // Create response CloudEvent with correlation to original event
            CloudEvent responseCloudEvent = cloudEventFactory.createCartonizationResponse(
                responseEvent.getRequestId(), responseEvent);
            
            // Add correlation ID from original event
            if (originalEvent.getExtension("correlationid") != null) {
                responseCloudEvent = cloudEventFactory.addCorrelation(
                    responseCloudEvent, originalEvent.getExtension("correlationid").toString());
            }

            // Publish to response topic
            String responseTopic = "${app.kafka.topics.cartonization-responses}";
            cloudEventPublisher.publishCloudEvent(responseTopic, responseEvent.getRequestId(), responseCloudEvent);
            
            log.debug("Published cartonization response CloudEvent for request: {}", responseEvent.getRequestId());
            
        } catch (Exception e) {
            log.error("Failed to publish cartonization response CloudEvent for request: {}", 
                     responseEvent.getRequestId(), e);
        }
    }

    private void handleErrorResponse(CloudEvent originalEvent, CartonizationRequestEvent requestEvent, 
                                   Exception error, long processingTimeMs) {
        try {
            if (requestEvent != null) {
                CartonizationResponseEvent errorResponse = eventMapper.toErrorResponse(
                    requestEvent, error.getMessage(), processingTimeMs);

                // Create error CloudEvent
                CloudEvent errorCloudEvent = cloudEventFactory.createCartonizationFailed(
                    requestEvent.getRequestId(), errorResponse);
                
                // Add correlation ID from original event
                if (originalEvent.getExtension("correlationid") != null) {
                    errorCloudEvent = cloudEventFactory.addCorrelation(
                        errorCloudEvent, originalEvent.getExtension("correlationid").toString());
                }

                // Publish to response topic
                String responseTopic = "${app.kafka.topics.cartonization-responses}";
                cloudEventPublisher.publishCloudEvent(responseTopic, requestEvent.getRequestId(), errorCloudEvent);
            }
        } catch (Exception e) {
            log.error("Failed to handle error response CloudEvent", e);
        }
    }
}