package vroong.laas.readmodel.common.repository.mongo;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoDB Document for Order Projection
 * 
 * 구조: OrderAggregate의 nested 구조를 그대로 반영
 */
@Document(collection = "order_projections")
@Getter
@Builder
public class OrderDocument {
    
    @Id
    private final String id;
    
    // Root Level IDs
    @Indexed
    private final Long orderId;
    private final Long dispatchId;
    private final Long deliveryId;
    
    // Nested: Order 정보
    private final OrderInfoDocument orderInfo;
    
    // Nested: Dispatch 정보
    private final DispatchInfoDocument dispatchInfo;
    
    // Nested: Delivery 정보
    private final DeliveryInfoDocument deliveryInfo;
    
    // Projection 메타데이터
    @Indexed
    private final Instant createdAt;
    @Indexed
    private final Instant updatedAt;
    
    // ========================================
    // Nested Document Classes
    // ========================================
    
    @Getter
    @Builder
    public static class OrderInfoDocument {
        @Indexed
        private final String orderNumber;
        @Indexed
        private final String orderStatus;
        private final OrderLocationDocument originLocation;
        private final OrderLocationDocument destinationLocation;
        private final List<OrderItemDocument> items;
        @Indexed
        private final Instant orderedAt;
    }
    
    @Getter
    @Builder
    public static class DispatchInfoDocument {
        @Indexed
        private final Long agentId;
        private final BigDecimal suggestedFee;
        @Indexed
        private final Instant dispatchedAt;
    }
    
    @Getter
    @Builder
    public static class DeliveryInfoDocument {
        private final String deliveryNumber;
        @Indexed
        private final Long agentId;
        private final BigDecimal deliveryFee;
        @Indexed
        private final String deliveryStatus;
        @Indexed
        private final Instant deliveryStartedAt;
        @Indexed
        private final Instant deliveryPickedUpAt;
        @Indexed
        private final Instant deliveryDeliveredAt;
        private final Instant deliveryCancelledAt;
    }
    
    @Getter
    @Builder
    public static class OrderLocationDocument {
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
    public static class OrderItemDocument {
        private final String itemName;
        private final Integer quantity;
        private final BigDecimal price;
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    public static String generateId(Long orderId) {
        return "order_" + orderId;
    }
    
    /**
     * OrderAggregate → OrderDocument 변환
     */
    public static OrderDocument from(OrderAggregate aggregate) {
        return OrderDocument.builder()
                .id(generateId(aggregate.getOrderId()))
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
    
    private static OrderInfoDocument convertOrderInfo(OrderAggregate.OrderInfo orderInfo) {
        return OrderInfoDocument.builder()
                .orderNumber(orderInfo.getOrderNumber())
                .orderStatus(orderInfo.getOrderStatus())
                .originLocation(orderInfo.getOriginLocation() != null ? 
                    convertLocation(orderInfo.getOriginLocation()) : null)
                .destinationLocation(orderInfo.getDestinationLocation() != null ? 
                    convertLocation(orderInfo.getDestinationLocation()) : null)
                .items(orderInfo.getItems() != null ?
                    orderInfo.getItems().stream()
                            .map(OrderDocument::convertItem)
                            .collect(Collectors.toList()) : null)
                .orderedAt(orderInfo.getOrderedAt())
                .build();
    }
    
    private static DispatchInfoDocument convertDispatchInfo(OrderAggregate.DispatchInfo dispatchInfo) {
        return DispatchInfoDocument.builder()
                .agentId(dispatchInfo.getAgentId())
                .suggestedFee(dispatchInfo.getSuggestedFee())
                .dispatchedAt(dispatchInfo.getDispatchedAt())
                .build();
    }
    
    private static DeliveryInfoDocument convertDeliveryInfo(OrderAggregate.DeliveryInfo deliveryInfo) {
        return DeliveryInfoDocument.builder()
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
    
    private static OrderLocationDocument convertLocation(OrderAggregate.OrderLocation location) {
        return OrderLocationDocument.builder()
                .contactName(location.getContactName())
                .contactPhoneNumber(location.getContactPhoneNumber())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .jibunAddress(location.getJibunAddress())
                .roadAddress(location.getRoadAddress())
                .detailAddress(location.getDetailAddress())
                .build();
    }
    
    private static OrderItemDocument convertItem(OrderAggregate.OrderItem item) {
        return OrderItemDocument.builder()
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
    
    /**
     * OrderDocument → OrderAggregate 변환
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
    
    private static OrderAggregate.OrderInfo convertToOrderInfo(OrderInfoDocument doc) {
        return OrderAggregate.OrderInfo.builder()
                .orderNumber(doc.getOrderNumber())
                .orderStatus(doc.getOrderStatus())
                .originLocation(doc.getOriginLocation() != null ? 
                    convertToLocation(doc.getOriginLocation()) : null)
                .destinationLocation(doc.getDestinationLocation() != null ? 
                    convertToLocation(doc.getDestinationLocation()) : null)
                .items(doc.getItems() != null ?
                    doc.getItems().stream()
                            .map(OrderDocument::convertToItem)
                            .collect(Collectors.toList()) : null)
                .orderedAt(doc.getOrderedAt())
                .build();
    }
    
    private static OrderAggregate.DispatchInfo convertToDispatchInfo(DispatchInfoDocument doc) {
        return OrderAggregate.DispatchInfo.builder()
                .agentId(doc.getAgentId())
                .suggestedFee(doc.getSuggestedFee())
                .dispatchedAt(doc.getDispatchedAt())
                .build();
    }
    
    private static OrderAggregate.DeliveryInfo convertToDeliveryInfo(DeliveryInfoDocument doc) {
        return OrderAggregate.DeliveryInfo.builder()
                .deliveryNumber(doc.getDeliveryNumber())
                .agentId(doc.getAgentId())
                .deliveryFee(doc.getDeliveryFee())
                .deliveryStatus(doc.getDeliveryStatus())
                .deliveryStartedAt(doc.getDeliveryStartedAt())
                .deliveryPickedUpAt(doc.getDeliveryPickedUpAt())
                .deliveryDeliveredAt(doc.getDeliveryDeliveredAt())
                .deliveryCancelledAt(doc.getDeliveryCancelledAt())
                .build();
    }
    
    private static OrderAggregate.OrderLocation convertToLocation(OrderLocationDocument doc) {
        return OrderAggregate.OrderLocation.builder()
                .contactName(doc.getContactName())
                .contactPhoneNumber(doc.getContactPhoneNumber())
                .latitude(doc.getLatitude())
                .longitude(doc.getLongitude())
                .jibunAddress(doc.getJibunAddress())
                .roadAddress(doc.getRoadAddress())
                .detailAddress(doc.getDetailAddress())
                .build();
    }
    
    private static OrderAggregate.OrderItem convertToItem(OrderItemDocument doc) {
        return OrderAggregate.OrderItem.builder()
                .itemName(doc.getItemName())
                .quantity(doc.getQuantity())
                .price(doc.getPrice())
                .build();
    }
}