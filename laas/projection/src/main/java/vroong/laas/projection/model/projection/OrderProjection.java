package vroong.laas.projection.model.projection;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class OrderProjection {
    
    private final Long orderId;
    private final String orderNumber;
    private final String orderStatus;
    private final OrderLocation originLocation;
    private final OrderLocation destinationLocation;
    private final List<OrderItem> items;
    
    private final Instant orderedAt;
    
    // Dispatch 정보
    private final Long dispatchId;
    private final Long agentId;
    private final BigDecimal deliveryFee;
    private final Instant dispatchedAt;
    
    // Delivery 정보
    private final Long deliveryId;
    private final String deliveryStatus;
    private final Instant deliveryStartedAt;
    private final Instant deliveryPickedUpAt;
    private final Instant deliveryDeliveredAt;
    
    // Projection 메타데이터
    private final Instant createdAt;
    private final Instant updatedAt;
    
    @Getter
    @Builder
    public static class OrderLocation {
        private final String contactName;
        private final String contactPhoneNumber;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String jibunAddress;
        private final String roadAddress;
        private final String detailAddress;
    }
    
    @Getter
    @Builder
    public static class OrderItem {
        private final String itemName;
        private final Integer quantity;
        private final BigDecimal price;
    }
    
    public enum DeliveryStatus {
        NOT_STARTED,
        STARTED,
        PICKED_UP,
        DELIVERED
    }
}