package com.paklog.cartonization.infrastructure.adapter.in.messaging.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class CloudEventFactory {

    private final String serviceSource;
    private final String serviceVersion;

    public CloudEventFactory(@Value("${spring.application.name}") String serviceName,
                           @Value("${app.version:1.0.0}") String serviceVersion) {
        this.serviceSource = "//paklog.com/services/" + serviceName;
        this.serviceVersion = serviceVersion;
    }

    public CloudEvent createCartonizationRequest(String requestId, Object data) {
        return createCloudEvent(
            CloudEventTypes.CARTONIZATION_REQUEST,
            requestId,
            data,
            "application/json"
        );
    }

    public CloudEvent createCartonizationResponse(String requestId, Object data) {
        return createCloudEvent(
            CloudEventTypes.CARTONIZATION_RESPONSE,
            requestId,
            data,
            "application/json"
        );
    }

    public CloudEvent createCartonizationFailed(String requestId, Object errorData) {
        return createCloudEvent(
            CloudEventTypes.CARTONIZATION_FAILED,
            requestId,
            errorData,
            "application/json"
        );
    }

    public CloudEvent createCartonCreated(String cartonId, Object data) {
        return createCloudEvent(
            CloudEventTypes.CARTON_CREATED,
            cartonId,
            data,
            "application/json"
        );
    }

    public CloudEvent createCartonUpdated(String cartonId, Object data) {
        return createCloudEvent(
            CloudEventTypes.CARTON_UPDATED,
            cartonId,
            data,
            "application/json"
        );
    }

    public CloudEvent createCartonDeactivated(String cartonId, Object data) {
        return createCloudEvent(
            CloudEventTypes.CARTON_DEACTIVATED,
            cartonId,
            data,
            "application/json"
        );
    }

    public CloudEvent createPackingSolutionCalculated(String solutionId, Object data) {
        return createCloudEvent(
            CloudEventTypes.PACKING_SOLUTION_CALCULATED,
            solutionId,
            data,
            "application/json"
        );
    }

    public CloudEvent createValidationFailed(String requestId, Object errorData) {
        return createCloudEvent(
            CloudEventTypes.VALIDATION_FAILED,
            requestId,
            errorData,
            "application/json"
        );
    }

    public CloudEvent createProductCatalogRequested(String requestId, Object data) {
        return createCloudEvent(
            CloudEventTypes.PRODUCT_CATALOG_REQUESTED,
            requestId,
            data,
            "application/json"
        );
    }

    public CloudEvent createCustomEvent(String eventType, String subject, Object data, String dataContentType) {
        return createCloudEvent(eventType, subject, data, dataContentType);
    }

    private CloudEvent createCloudEvent(String eventType, String subject, Object data, String dataContentType) {
        CloudEventBuilder builder = CloudEventBuilder.v1()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create(serviceSource))
            .withType(eventType)
            .withTime(OffsetDateTime.now(ZoneOffset.UTC))
            .withDataContentType(dataContentType)
            .withExtension("serviceversion", serviceVersion)
            .withExtension("correlationid", UUID.randomUUID().toString());

        if (subject != null && !subject.trim().isEmpty()) {
            builder.withSubject(subject);
        }

        if (data != null) {
            builder.withData(data.toString().getBytes());
        }

        return builder.build();
    }

    public CloudEvent createEventFromTemplate(CloudEvent template, String newEventType, Object newData) {
        CloudEventBuilder builder = CloudEventBuilder.v1(template)
            .withId(UUID.randomUUID().toString())
            .withType(newEventType)
            .withTime(OffsetDateTime.now(ZoneOffset.UTC));

        if (newData != null) {
            builder.withData(newData.toString().getBytes());
        }

        return builder.build();
    }

    public CloudEvent addCorrelation(CloudEvent event, String correlationId) {
        return CloudEventBuilder.v1(event)
            .withExtension("correlationid", correlationId)
            .build();
    }

    public CloudEvent addTracing(CloudEvent event, String traceId, String spanId) {
        return CloudEventBuilder.v1(event)
            .withExtension("traceid", traceId)
            .withExtension("spanid", spanId)
            .build();
    }
}