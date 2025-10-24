package vroong.laas.readmodel.common.repository.redis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Redis Model for Order Projection
 * 
 * 구조: OrderAggregate의 nested 구조를 그대로 반영
 */
@Getter
@Builder
public class OrderRedisModel {
    
    // Root Level IDs
    private final Long orderId;
    private final Long dispatchId;
    private final Long deliveryId;
    
    // Nested: Order 정보
    private final OrderInfoRedis orderInfo;
    
    // Nested: Dispatch 정보
    private final DispatchInfoRedis dispatchInfo;
    
    // Nested: Delivery 정보
    private final DeliveryInfoRedis deliveryInfo;
    
    // Projection 메타데이터
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant updatedAt;
    
    // ========================================
    // Nested Redis Classes
    // ========================================
    
    @Getter
    @Builder
    public static class OrderInfoRedis {
        private final String orderNumber;
        private final String orderStatus;
        private final OrderLocationRedis originLocation;
        private final OrderLocationRedis destinationLocation;
        private final List<OrderItemRedis> items;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final Instant orderedAt;
    }
    
    @Getter
    @Builder
    public static class DispatchInfoRedis {
        private final Long agentId;
        private final BigDecimal suggestedFee;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final Instant dispatchedAt;
    }
    
    @Getter
    @Builder
    public static class DeliveryInfoRedis {
        private final String deliveryNumber;
        private final Long agentId;
        private final BigDecimal deliveryFee;
        private final String deliveryStatus;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final Instant deliveryStartedAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final Instant deliveryPickedUpAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final Instant deliveryDeliveredAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private final Instant deliveryCancelledAt;
    }
    
    @Getter
    @Builder
    public static class OrderLocationRedis {
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
    public static class OrderItemRedis {
        private final String itemName;
        private final Integer quantity;
        private final BigDecimal price;
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    public static String generateKey(Long orderId) {
        return "order:projection:" + orderId;
    }
    
    /**
     * OrderAggregate → OrderRedisModel 변환
     */
    public static OrderRedisModel from(OrderAggregate aggregate) {
        return OrderRedisModel.builder()
                .orderId(aggregate.getOrderId())
                .dispatchId(aggregate.getDispatchId())
                .deliveryId(aggregate.getDeliveryId())
                .orderInfo(aggregate.getOrderInfo() != null ? convertOrderInfo(aggregate.getOrderInfo()) : null)
                .dispatchInfo(aggregate.getDispatchInfo() != null ? convertDispatchInfo(aggregate.getDispatchInfo()) : null)
                .deliveryInfo(aggregate.getDeliveryInfo() != null ? convertDeliveryInfo(aggregate.getDeliveryInfo()) : null)
                .createdAt(aggregate.getCreatedAt())
                .updatedAt(aggregate.getUpdatedAt())
                .build();
    }
    
    private static OrderInfoRedis convertOrderInfo(OrderAggregate.OrderInfo orderInfo) {
        return OrderInfoRedis.builder()
                .orderNumber(orderInfo.getOrderNumber())
                .orderStatus(orderInfo.getOrderStatus())
                .originLocation(orderInfo.getOriginLocation() != null ? 
                    convertLocation(orderInfo.getOriginLocation()) : null)
                .destinationLocation(orderInfo.getDestinationLocation() != null ? 
                    convertLocation(orderInfo.getDestinationLocation()) : null)
                .items(orderInfo.getItems() != null ?
                    orderInfo.getItems().stream()
                            .map(OrderRedisModel::convertItem)
                            .collect(Collectors.toList()) : null)
                .orderedAt(orderInfo.getOrderedAt())
                .build();
    }
    
    private static DispatchInfoRedis convertDispatchInfo(OrderAggregate.DispatchInfo dispatchInfo) {
        return DispatchInfoRedis.builder()
                .agentId(dispatchInfo.getAgentId())
                .suggestedFee(dispatchInfo.getSuggestedFee())
                .dispatchedAt(dispatchInfo.getDispatchedAt())
                .build();
    }
    
    private static DeliveryInfoRedis convertDeliveryInfo(OrderAggregate.DeliveryInfo deliveryInfo) {
        return DeliveryInfoRedis.builder()
                .deliveryNumber(deliveryInfo.getDeliveryNumber())
                .agentId(deliveryInfo.getAgentId())
                .deliveryFee(deliveryInfo.getDeliveryFee())
                .deliveryStatus(deliveryInfo.getDeliveryStatus())
                .deliveryStartedAt(deliveryInfo.getDeliveryStartedAt())
                .deliveryPickedUpAt(deliveryInfo.getDeliveryPickedUpAt())
                .deliveryDeliveredAt(deliveryInfo.getDeliveryDeliveredAt())
                .deliveryCancelledAt(deliveryInfo.getDeliveryCancelledAt())
                .build();
    }
    
    private static OrderLocationRedis convertLocation(OrderAggregate.OrderLocation location) {
        return OrderLocationRedis.builder()
                .contactName(location.getContactName())
                .contactPhoneNumber(location.getContactPhoneNumber())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .jibunAddress(location.getJibunAddress())
                .roadAddress(location.getRoadAddress())
                .detailAddress(location.getDetailAddress())
                .build();
    }
    
    private static OrderItemRedis convertItem(OrderAggregate.OrderItem item) {
        return OrderItemRedis.builder()
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
    
    /**
     * OrderRedisModel → OrderAggregate 변환
     */
    public OrderAggregate toAggregate() {
        return OrderAggregate.builder()
                .orderId(this.orderId)
                .dispatchId(this.dispatchId)
                .deliveryId(this.deliveryId)
                .orderInfo(this.orderInfo != null ? convertToOrderInfo(this.orderInfo) : null)
                .dispatchInfo(this.dispatchInfo != null ? convertToDispatchInfo(this.dispatchInfo) : null)
                .deliveryInfo(this.deliveryInfo != null ? convertToDeliveryInfo(this.deliveryInfo) : null)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    
    private static OrderAggregate.OrderInfo convertToOrderInfo(OrderInfoRedis redis) {
        return OrderAggregate.OrderInfo.builder()
                .orderNumber(redis.getOrderNumber())
                .orderStatus(redis.getOrderStatus())
                .originLocation(redis.getOriginLocation() != null ? 
                    convertToLocation(redis.getOriginLocation()) : null)
                .destinationLocation(redis.getDestinationLocation() != null ? 
                    convertToLocation(redis.getDestinationLocation()) : null)
                .items(redis.getItems() != null ?
                    redis.getItems().stream()
                            .map(OrderRedisModel::convertToItem)
                            .collect(Collectors.toList()) : null)
                .orderedAt(redis.getOrderedAt())
                .build();
    }
    
    private static OrderAggregate.DispatchInfo convertToDispatchInfo(DispatchInfoRedis redis) {
        return OrderAggregate.DispatchInfo.builder()
                .agentId(redis.getAgentId())
                .suggestedFee(redis.getSuggestedFee())
                .dispatchedAt(redis.getDispatchedAt())
                .build();
    }
    
    private static OrderAggregate.DeliveryInfo convertToDeliveryInfo(DeliveryInfoRedis redis) {
        return OrderAggregate.DeliveryInfo.builder()
                .deliveryNumber(redis.getDeliveryNumber())
                .agentId(redis.getAgentId())
                .deliveryFee(redis.getDeliveryFee())
                .deliveryStatus(redis.getDeliveryStatus())
                .deliveryStartedAt(redis.getDeliveryStartedAt())
                .deliveryPickedUpAt(redis.getDeliveryPickedUpAt())
                .deliveryDeliveredAt(redis.getDeliveryDeliveredAt())
                .deliveryCancelledAt(redis.getDeliveryCancelledAt())
                .build();
    }
    
    private static OrderAggregate.OrderLocation convertToLocation(OrderLocationRedis redis) {
        return OrderAggregate.OrderLocation.builder()
                .contactName(redis.getContactName())
                .contactPhoneNumber(redis.getContactPhoneNumber())
                .latitude(redis.getLatitude())
                .longitude(redis.getLongitude())
                .jibunAddress(redis.getJibunAddress())
                .roadAddress(redis.getRoadAddress())
                .detailAddress(redis.getDetailAddress())
                .build();
    }
    
    private static OrderAggregate.OrderItem convertToItem(OrderItemRedis redis) {
        return OrderAggregate.OrderItem.builder()
                .itemName(redis.getItemName())
                .quantity(redis.getQuantity())
                .price(redis.getPrice())
                .build();
    }
}
