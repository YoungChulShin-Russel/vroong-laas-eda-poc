package vroong.laas.readmodel.common.repository.redis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import vroong.laas.readmodel.common.model.OrderInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderRedisModel {
    
    private final Long orderId;
    private final String orderNumber;
    private final String orderStatus;
    private final OrderLocationRedis originLocation;
    private final OrderLocationRedis destinationLocation;
    private final List<OrderItemRedis> items;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant orderedAt;
    
    private final Long dispatchId;
    private final Long agentId;
    private final BigDecimal deliveryFee;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant dispatchedAt;
    
    private final Long deliveryId;
    private final String deliveryStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant deliveryStartedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant deliveryPickedUpAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant deliveryDeliveredAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant updatedAt;
    
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
    
    public static String generateKey(Long orderId) {
        return "order:projection:" + orderId;
    }
    
    public static OrderRedisModel from(OrderInfo projection) {
        return OrderRedisModel.builder()
                .orderId(projection.getOrderId())
                .orderNumber(projection.getOrderNumber())
                .orderStatus(projection.getOrderStatus())
                .originLocation(projection.getOriginLocation() != null ? 
                    OrderLocationRedis.builder()
                            .contactName(projection.getOriginLocation().getContactName())
                            .contactPhoneNumber(projection.getOriginLocation().getContactPhoneNumber())
                            .latitude(projection.getOriginLocation().getLatitude())
                            .longitude(projection.getOriginLocation().getLongitude())
                            .jibunAddress(projection.getOriginLocation().getJibunAddress())
                            .roadAddress(projection.getOriginLocation().getRoadAddress())
                            .detailAddress(projection.getOriginLocation().getDetailAddress())
                            .build() : null)
                .destinationLocation(projection.getDestinationLocation() != null ?
                    OrderLocationRedis.builder()
                            .contactName(projection.getDestinationLocation().getContactName())
                            .contactPhoneNumber(projection.getDestinationLocation().getContactPhoneNumber())
                            .latitude(projection.getDestinationLocation().getLatitude())
                            .longitude(projection.getDestinationLocation().getLongitude())
                            .jibunAddress(projection.getDestinationLocation().getJibunAddress())
                            .roadAddress(projection.getDestinationLocation().getRoadAddress())
                            .detailAddress(projection.getDestinationLocation().getDetailAddress())
                            .build() : null)
                .items(projection.getItems() != null ?
                    projection.getItems().stream()
                            .map(item -> OrderItemRedis.builder()
                                    .itemName(item.getItemName())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .build())
                            .collect(Collectors.toList()) : null)
                .orderedAt(projection.getOrderedAt())
                .dispatchId(projection.getDispatchId())
                .agentId(projection.getAgentId())
                .deliveryFee(projection.getDeliveryFee())
                .dispatchedAt(projection.getDispatchedAt())
                .deliveryId(projection.getDeliveryId())
                .deliveryStatus(projection.getDeliveryStatus())
                .deliveryStartedAt(projection.getDeliveryStartedAt())
                .deliveryPickedUpAt(projection.getDeliveryPickedUpAt())
                .deliveryDeliveredAt(projection.getDeliveryDeliveredAt())
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .build();
    }
    
    public OrderInfo toProjection() {
        return OrderInfo.builder()
                .orderId(this.orderId)
                .orderNumber(this.orderNumber)
                .orderStatus(this.orderStatus)
                .originLocation(this.originLocation != null ?
                    OrderInfo.OrderLocation.builder()
                            .contactName(this.originLocation.getContactName())
                            .contactPhoneNumber(this.originLocation.getContactPhoneNumber())
                            .latitude(this.originLocation.getLatitude())
                            .longitude(this.originLocation.getLongitude())
                            .jibunAddress(this.originLocation.getJibunAddress())
                            .roadAddress(this.originLocation.getRoadAddress())
                            .detailAddress(this.originLocation.getDetailAddress())
                            .build() : null)
                .destinationLocation(this.destinationLocation != null ?
                    OrderInfo.OrderLocation.builder()
                            .contactName(this.destinationLocation.getContactName())
                            .contactPhoneNumber(this.destinationLocation.getContactPhoneNumber())
                            .latitude(this.destinationLocation.getLatitude())
                            .longitude(this.destinationLocation.getLongitude())
                            .jibunAddress(this.destinationLocation.getJibunAddress())
                            .roadAddress(this.destinationLocation.getRoadAddress())
                            .detailAddress(this.destinationLocation.getDetailAddress())
                            .build() : null)
                .items(this.items != null ?
                    this.items.stream()
                            .map(item -> OrderInfo.OrderItem.builder()
                                    .itemName(item.getItemName())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .build())
                            .collect(Collectors.toList()) : null)
                .orderedAt(this.orderedAt)
                .dispatchId(this.dispatchId)
                .agentId(this.agentId)
                .deliveryFee(this.deliveryFee)
                .dispatchedAt(this.dispatchedAt)
                .deliveryId(this.deliveryId)
                .deliveryStatus(this.deliveryStatus)
                .deliveryStartedAt(this.deliveryStartedAt)
                .deliveryPickedUpAt(this.deliveryPickedUpAt)
                .deliveryDeliveredAt(this.deliveryDeliveredAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}