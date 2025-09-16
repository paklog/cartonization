package com.paklog.cartonization.infrastructure.adapter.in.messaging.cloudevents;

public final class CloudEventTypes {
    
    public static final String EVENT_TYPE_PREFIX = "com.paklog.cartonization";
    
    // Request Events
    public static final String CARTONIZATION_REQUEST = EVENT_TYPE_PREFIX + ".request.cartonization";
    public static final String CARTON_MANAGEMENT_REQUEST = EVENT_TYPE_PREFIX + ".request.carton-management";
    
    // Response Events
    public static final String CARTONIZATION_RESPONSE = EVENT_TYPE_PREFIX + ".response.cartonization";
    public static final String CARTON_MANAGEMENT_RESPONSE = EVENT_TYPE_PREFIX + ".response.carton-management";
    
    // Domain Events
    public static final String CARTON_CREATED = EVENT_TYPE_PREFIX + ".domain.carton.created";
    public static final String CARTON_UPDATED = EVENT_TYPE_PREFIX + ".domain.carton.updated";
    public static final String CARTON_DEACTIVATED = EVENT_TYPE_PREFIX + ".domain.carton.deactivated";
    public static final String PACKING_SOLUTION_CALCULATED = EVENT_TYPE_PREFIX + ".domain.packing-solution.calculated";
    
    // Error Events
    public static final String CARTONIZATION_FAILED = EVENT_TYPE_PREFIX + ".error.cartonization-failed";
    public static final String VALIDATION_FAILED = EVENT_TYPE_PREFIX + ".error.validation-failed";
    
    // Integration Events
    public static final String PRODUCT_CATALOG_REQUESTED = EVENT_TYPE_PREFIX + ".integration.product-catalog.requested";
    public static final String PRODUCT_CATALOG_RECEIVED = EVENT_TYPE_PREFIX + ".integration.product-catalog.received";
    
    // System Events
    public static final String SERVICE_STARTED = EVENT_TYPE_PREFIX + ".system.service.started";
    public static final String SERVICE_STOPPED = EVENT_TYPE_PREFIX + ".system.service.stopped";
    public static final String HEALTH_CHECK = EVENT_TYPE_PREFIX + ".system.health.check";
    
    private CloudEventTypes() {
        // Utility class
    }
}