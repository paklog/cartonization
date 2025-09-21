package com.paklog.cartonization;

import com.paklog.cartonization.domain.model.valueobject.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
class CartonizationIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
        DockerImageName.parse("mongo:7.0")
    );

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        // Disable Redis for tests
        registry.add("spring.cache.type", () -> "simple");
        registry.add("spring.data.redis.host", () -> "disabled");
    }

    @Test
    void contextLoads() {
        // Test that the application context loads successfully with Testcontainers
        assertThat(mongoDBContainer.isRunning()).isTrue();
        assertThat(kafkaContainer.isRunning()).isTrue();
    }

    @Test
    void shouldCreateAndValidateValueObjects() {
        // Test value object creation and validation
        CartonId cartonId = CartonId.generate();
        assertThat(cartonId).isNotNull();
        assertThat(cartonId.getValue()).isNotBlank();

        DimensionSet dimensions = new DimensionSet(
            BigDecimal.valueOf(12), BigDecimal.valueOf(8), BigDecimal.valueOf(6),
            DimensionUnit.INCHES
        );
        assertThat(dimensions.getLength()).isEqualTo(BigDecimal.valueOf(12));
        assertThat(dimensions.canContain(dimensions)).isTrue();

        Weight weight = new Weight(BigDecimal.valueOf(25), WeightUnit.POUNDS);
        assertThat(weight.getValue()).isEqualTo(BigDecimal.valueOf(25));
        assertThat(weight.getUnit()).isEqualTo(WeightUnit.POUNDS);
    }

    @Test
    void shouldValidateBusinessRules() {
        // Test carton creation with business rules
        DimensionSet dimensions = new DimensionSet(
            BigDecimal.valueOf(12), BigDecimal.valueOf(8), BigDecimal.valueOf(6),
            DimensionUnit.INCHES
        );
        Weight maxWeight = new Weight(BigDecimal.valueOf(25), WeightUnit.POUNDS);

        var carton = com.paklog.cartonization.domain.model.aggregate.Carton.create(
            "Test Box", dimensions, maxWeight
        );

        assertThat(carton).isNotNull();
        assertThat(carton.getName()).isEqualTo("Test Box");
        assertThat(carton.getStatus()).isEqualTo(CartonStatus.ACTIVE);

        // Test item fitting logic
        DimensionSet itemDimensions = new DimensionSet(
            BigDecimal.valueOf(10), BigDecimal.valueOf(6), BigDecimal.valueOf(4),
            DimensionUnit.INCHES
        );
        Weight itemWeight = new Weight(BigDecimal.valueOf(10), WeightUnit.POUNDS);

        boolean canFit = carton.canFitItem(itemDimensions, itemWeight);
        assertThat(canFit).isTrue();
    }
}