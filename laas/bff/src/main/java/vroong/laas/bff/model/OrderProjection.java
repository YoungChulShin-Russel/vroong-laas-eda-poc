package vroong.laas.bff.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Order Projection 도메인 모델
 * 
 * 순수한 도메인 모델 - 인프라 의존성 없음
 * - API Response로 직접 사용
 * - MongoDB 조회 결과를 필드명 기반으로 자동 매핑
 * - Redis 직렬화/역직렬화 지원
 * 
 * BFF는 조회만 하므로 @Document, @Id, @Indexed 등의 어노테이션 불필요
 */
@Getter
@Builder(toBuilder = true)
@JsonDeserialize(builder = OrderProjection.OrderProjectionBuilder.class)
public class OrderProjection {
    
    private final String id;
    private final Long orderId;
    private final String orderNumber;
    private final String orderStatus;
    private final OrderLocation originLocation;
    private final OrderLocation destinationLocation;
    private final List<OrderItem> items;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant orderedAt;
    
    // Dispatch 정보
    private final Long dispatchId;
    private final Long agentId;
    private final BigDecimal deliveryFee;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant dispatchedAt;
    
    // Delivery 정보
    private final Long deliveryId;
    private final String deliveryStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant deliveryStartedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant deliveryPickedUpAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant deliveryDeliveredAt;
    
    // Projection 메타데이터
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant updatedAt;
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class OrderProjectionBuilder {
    }
    
    @Getter
    @Builder
    @JsonDeserialize(builder = OrderLocation.OrderLocationBuilder.class)
    public static class OrderLocation {
        private final String contactName;
        private final String contactPhoneNumber;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String jibunAddress;
        private final String roadAddress;
        private final String detailAddress;
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class OrderLocationBuilder {
        }
    }
    
    @Getter
    @Builder
    @JsonDeserialize(builder = OrderItem.OrderItemBuilder.class)
    public static class OrderItem {
        private final String itemName;
        private final Integer quantity;
        private final BigDecimal price;
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class OrderItemBuilder {
        }
    }
}

