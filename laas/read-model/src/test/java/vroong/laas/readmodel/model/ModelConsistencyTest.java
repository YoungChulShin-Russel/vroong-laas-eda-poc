package vroong.laas.readmodel.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vroong.laas.readmodel.model.document.OrderDocument;
import vroong.laas.readmodel.model.projection.OrderProjection;
import vroong.laas.readmodel.model.redis.OrderRedisModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("모델 간 데이터 일관성 테스트")
class ModelConsistencyTest {

    @Test
    @DisplayName("Redis와 MongoDB 모델이 동일한 데이터를 유지해야 한다")
    void shouldMaintainDataConsistencyBetweenRedisAndMongo() {
        // Given
        OrderProjection originalProjection = createSampleProjection();
        
        // When
        OrderRedisModel redisModel = OrderRedisModel.from(originalProjection);
        OrderDocument mongoDocument = OrderDocument.from(originalProjection);
        
        OrderProjection fromRedis = redisModel.toProjection();
        OrderProjection fromMongo = mongoDocument.toProjection();
        
        // Then
        assertThat(fromRedis).usingRecursiveComparison()
                .isEqualTo(fromMongo);
        
        assertThat(fromRedis).usingRecursiveComparison()
                .isEqualTo(originalProjection);
    }

    @Test
    @DisplayName("모든 필드가 올바르게 변환되어야 한다")
    void shouldConvertAllFieldsCorrectly() {
        // Given
        OrderProjection originalProjection = createSampleProjection();
        
        // When
        OrderRedisModel redisModel = OrderRedisModel.from(originalProjection);
        OrderDocument mongoDocument = OrderDocument.from(originalProjection);
        
        // Then - Redis Model 검증
        assertThat(redisModel.getOrderId()).isEqualTo(originalProjection.getOrderId());
        assertThat(redisModel.getOrderNumber()).isEqualTo(originalProjection.getOrderNumber());
        assertThat(redisModel.getOrderStatus()).isEqualTo(originalProjection.getOrderStatus());
        assertThat(redisModel.getOrderedAt()).isEqualTo(originalProjection.getOrderedAt());
        assertThat(redisModel.getAgentId()).isEqualTo(originalProjection.getAgentId());
        assertThat(redisModel.getDeliveryStatus()).isEqualTo(originalProjection.getDeliveryStatus());
        
        // Then - MongoDB Document 검증
        assertThat(mongoDocument.getOrderId()).isEqualTo(originalProjection.getOrderId());
        assertThat(mongoDocument.getOrderNumber()).isEqualTo(originalProjection.getOrderNumber());
        assertThat(mongoDocument.getOrderStatus()).isEqualTo(originalProjection.getOrderStatus());
        assertThat(mongoDocument.getOrderedAt()).isEqualTo(originalProjection.getOrderedAt());
        assertThat(mongoDocument.getAgentId()).isEqualTo(originalProjection.getAgentId());
        assertThat(mongoDocument.getDeliveryStatus()).isEqualTo(originalProjection.getDeliveryStatus());
    }

    @Test
    @DisplayName("중첩 객체들도 올바르게 변환되어야 한다")
    void shouldConvertNestedObjectsCorrectly() {
        // Given
        OrderProjection originalProjection = createSampleProjection();
        
        // When
        OrderRedisModel redisModel = OrderRedisModel.from(originalProjection);
        OrderDocument mongoDocument = OrderDocument.from(originalProjection);
        
        OrderProjection fromRedis = redisModel.toProjection();
        OrderProjection fromMongo = mongoDocument.toProjection();
        
        // Then - Origin Location 검증
        assertThat(fromRedis.getOriginLocation()).usingRecursiveComparison()
                .isEqualTo(originalProjection.getOriginLocation());
        assertThat(fromMongo.getOriginLocation()).usingRecursiveComparison()
                .isEqualTo(originalProjection.getOriginLocation());
        
        // Then - Destination Location 검증
        assertThat(fromRedis.getDestinationLocation()).usingRecursiveComparison()
                .isEqualTo(originalProjection.getDestinationLocation());
        assertThat(fromMongo.getDestinationLocation()).usingRecursiveComparison()
                .isEqualTo(originalProjection.getDestinationLocation());
        
        // Then - Items 검증
        assertThat(fromRedis.getItems()).usingRecursiveComparison()
                .isEqualTo(originalProjection.getItems());
        assertThat(fromMongo.getItems()).usingRecursiveComparison()
                .isEqualTo(originalProjection.getItems());
    }

    @Test
    @DisplayName("null 값들도 올바르게 처리되어야 한다")
    void shouldHandleNullValuesCorrectly() {
        // Given
        OrderProjection projectionWithNulls = OrderProjection.builder()
                .orderId(1L)
                .orderNumber("ORDER-001")
                .orderStatus("CREATED")
                .originLocation(null)
                .destinationLocation(null)
                .items(null)
                .orderedAt(Instant.now())
                .dispatchId(null)
                .agentId(null)
                .deliveryFee(null)
                .dispatchedAt(null)
                .deliveryId(null)
                .deliveryStatus(null)
                .deliveryStartedAt(null)
                .deliveryPickedUpAt(null)
                .deliveryDeliveredAt(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        // When
        OrderRedisModel redisModel = OrderRedisModel.from(projectionWithNulls);
        OrderDocument mongoDocument = OrderDocument.from(projectionWithNulls);
        
        OrderProjection fromRedis = redisModel.toProjection();
        OrderProjection fromMongo = mongoDocument.toProjection();
        
        // Then
        assertThat(fromRedis).usingRecursiveComparison()
                .isEqualTo(fromMongo);
        assertThat(fromRedis).usingRecursiveComparison()
                .isEqualTo(projectionWithNulls);
    }

    @Test
    @DisplayName("MongoDB ID 생성이 일관되어야 한다")
    void shouldGenerateConsistentMongoId() {
        // Given
        Long orderId = 12345L;
        
        // When
        String mongoId = OrderDocument.generateId(orderId);
        
        // Then
        assertThat(mongoId).isEqualTo("order_12345");
    }

    @Test
    @DisplayName("Redis Key 생성이 일관되어야 한다")
    void shouldGenerateConsistentRedisKey() {
        // Given
        Long orderId = 12345L;
        
        // When
        String redisKey = OrderRedisModel.generateKey(orderId);
        
        // Then
        assertThat(redisKey).isEqualTo("order:projection:12345");
    }

    private OrderProjection createSampleProjection() {
        Instant now = Instant.now();
        
        return OrderProjection.builder()
                .orderId(1L)
                .orderNumber("ORDER-001")
                .orderStatus("CREATED")
                .originLocation(OrderProjection.OrderLocation.builder()
                        .contactName("김철수")
                        .contactPhoneNumber("010-1234-5678")
                        .latitude(new BigDecimal("37.5665"))
                        .longitude(new BigDecimal("126.9780"))
                        .jibunAddress("서울시 중구 태평로1가 31")
                        .roadAddress("서울시 중구 세종대로 110")
                        .detailAddress("시청 앞")
                        .build())
                .destinationLocation(OrderProjection.OrderLocation.builder()
                        .contactName("이영희")
                        .contactPhoneNumber("010-9876-5432")
                        .latitude(new BigDecimal("37.5172"))
                        .longitude(new BigDecimal("127.0473"))
                        .jibunAddress("서울시 강남구 삼성동 159")
                        .roadAddress("서울시 강남구 테헤란로 152")
                        .detailAddress("삼성역 근처")
                        .build())
                .items(List.of(
                        OrderProjection.OrderItem.builder()
                                .itemName("치킨")
                                .quantity(1)
                                .price(new BigDecimal("20000"))
                                .build(),
                        OrderProjection.OrderItem.builder()
                                .itemName("피자")
                                .quantity(2)
                                .price(new BigDecimal("15000"))
                                .build()
                ))
                .orderedAt(now.minusSeconds(3600))
                .dispatchId(100L)
                .agentId(200L)
                .deliveryFee(new BigDecimal("3000"))
                .dispatchedAt(now.minusSeconds(3000))
                .deliveryId(300L)
                .deliveryStatus("STARTED")
                .deliveryStartedAt(now.minusSeconds(2400))
                .deliveryPickedUpAt(now.minusSeconds(1800))
                .deliveryDeliveredAt(null)
                .createdAt(now.minusSeconds(3600))
                .updatedAt(now.minusSeconds(1800))
                .build();
    }
}