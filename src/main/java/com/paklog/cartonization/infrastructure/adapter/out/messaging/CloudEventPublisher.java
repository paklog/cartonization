package com.paklog.cartonization.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paklog.cartonization.application.port.out.EventPublisher;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.cloudevents.CloudEventFactory;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.kafka.CloudEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CloudEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CloudEventPublisher.class);
    
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;
    private final CloudEventFactory cloudEventFactory;
    private final ObjectMapper objectMapper;
    
    public CloudEventPublisher(KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate,
                             CloudEventFactory cloudEventFactory,
                             ObjectMapper objectMapper) {
        this.cloudEventKafkaTemplate = cloudEventKafkaTemplate;
        this.cloudEventFactory = cloudEventFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, Object event) {
        try {
            // Convert the event data to JSON
            String eventJson = objectMapper.writeValueAsString(event);
            
            // Create CloudEvent based on event type
            CloudEvent cloudEvent = createCloudEventFromPayload(event, eventJson);
            
            // Publish to Kafka
            cloudEventKafkaTemplate.send(topic, cloudEvent.getId(), cloudEvent);

            log.info("Published CloudEvent {} to topic {}", cloudEvent.getType(), topic);
            log.debug("CloudEvent details: id={}, source={}, subject={}", 
                     cloudEvent.getId(), cloudEvent.getSource(), cloudEvent.getSubject());

        } catch (Exception e) {
            log.error("Failed to publish CloudEvent for {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish CloudEvent", e);
        }
    }

    @Override
    public void publish(String topic, String key, Object event) {
        try {
            // Convert the event data to JSON
            String eventJson = objectMapper.writeValueAsString(event);
            
            // Create CloudEvent based on event type
            CloudEvent cloudEvent = createCloudEventFromPayload(event, eventJson);
            
            // Publish to Kafka with custom key
            cloudEventKafkaTemplate.send(topic, key, cloudEvent);

            log.info("Published CloudEvent {} with key {} to topic {}",
                    cloudEvent.getType(), key, topic);

        } catch (Exception e) {
            log.error("Failed to publish CloudEvent for {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish CloudEvent", e);
        }
    }

    public void publishCloudEvent(String topic, CloudEvent cloudEvent) {
        try {
            cloudEventKafkaTemplate.send(topic, cloudEvent.getId(), cloudEvent);
            
            log.info("Published CloudEvent {} to topic {}", cloudEvent.getType(), topic);
            log.debug("CloudEvent details: id={}, source={}, subject={}", 
                     cloudEvent.getId(), cloudEvent.getSource(), cloudEvent.getSubject());

        } catch (Exception e) {
            log.error("Failed to publish CloudEvent {}: {}", cloudEvent.getType(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish CloudEvent", e);
        }
    }

    public void publishCloudEvent(String topic, String key, CloudEvent cloudEvent) {
        try {
            cloudEventKafkaTemplate.send(topic, key, cloudEvent);
            
            log.info("Published CloudEvent {} with key {} to topic {}", cloudEvent.getType(), key, topic);

        } catch (Exception e) {
            log.error("Failed to publish CloudEvent {}: {}", cloudEvent.getType(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish CloudEvent", e);
        }
    }

    public void publishCartonizationResponse(String topic, String requestId, Object responseData) {
        CloudEvent cloudEvent = cloudEventFactory.createCartonizationResponse(requestId, responseData);
        publishCloudEvent(topic, requestId, cloudEvent);
    }

    public void publishCartonizationFailed(String topic, String requestId, Object errorData) {
        CloudEvent cloudEvent = cloudEventFactory.createCartonizationFailed(requestId, errorData);
        publishCloudEvent(topic, requestId, cloudEvent);
    }

    public void publishCartonCreated(String topic, String cartonId, Object cartonData) {
        CloudEvent cloudEvent = cloudEventFactory.createCartonCreated(cartonId, cartonData);
        publishCloudEvent(topic, cartonId, cloudEvent);
    }

    public void publishCartonUpdated(String topic, String cartonId, Object cartonData) {
        CloudEvent cloudEvent = cloudEventFactory.createCartonUpdated(cartonId, cartonData);
        publishCloudEvent(topic, cartonId, cloudEvent);
    }

    public void publishCartonDeactivated(String topic, String cartonId, Object cartonData) {
        CloudEvent cloudEvent = cloudEventFactory.createCartonDeactivated(cartonId, cartonData);
        publishCloudEvent(topic, cartonId, cloudEvent);
    }

    public void publishPackingSolutionCalculated(String topic, String solutionId, Object solutionData) {
        CloudEvent cloudEvent = cloudEventFactory.createPackingSolutionCalculated(solutionId, solutionData);
        publishCloudEvent(topic, solutionId, cloudEvent);
    }

    private CloudEvent createCloudEventFromPayload(Object event, String eventJson) {
        String eventClassName = event.getClass().getSimpleName();
        
        // Map event types to CloudEvent types
        String subject = extractSubjectFromEvent(event);
        
        return switch (eventClassName) {
            case "CartonizationRequestEvent" -> 
                cloudEventFactory.createCartonizationRequest(subject, eventJson);
            case "CartonizationResponseEvent" -> 
                cloudEventFactory.createCartonizationResponse(subject, eventJson);
            case "CartonCreatedEvent" -> 
                cloudEventFactory.createCartonCreated(subject, eventJson);
            case "CartonUpdatedEvent" -> 
                cloudEventFactory.createCartonUpdated(subject, eventJson);
            case "CartonDeactivatedEvent" -> 
                cloudEventFactory.createCartonDeactivated(subject, eventJson);
            case "PackingSolutionCalculatedEvent" -> 
                cloudEventFactory.createPackingSolutionCalculated(subject, eventJson);
            default -> 
                cloudEventFactory.createCustomEvent(
                    "com.paklog.cartonization.unknown." + eventClassName.toLowerCase(),
                    subject,
                    eventJson,
                    "application/json"
                );
        };
    }

    private String extractSubjectFromEvent(Object event) {
        // Use reflection to extract ID or relevant identifier from event
        try {
            var clazz = event.getClass();
            
            // Try common ID field names
            String[] idFields = {"requestId", "cartonId", "solutionId", "id", "getId"};
            
            for (String fieldName : idFields) {
                try {
                    if (fieldName.startsWith("get")) {
                        var method = clazz.getMethod(fieldName);
                        Object result = method.invoke(event);
                        if (result != null) {
                            return result.toString();
                        }
                    } else {
                        var field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object result = field.get(event);
                        if (result != null) {
                            return result.toString();
                        }
                    }
                } catch (Exception ignored) {
                    // Continue to next field
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract subject from event: {}", e.getMessage());
        }
        
        return null; // Subject is optional in CloudEvents
    }
}