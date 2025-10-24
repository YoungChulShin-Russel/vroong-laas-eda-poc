package vroong.laas.readmodel.query.controller.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import vroong.laas.readmodel.common.model.OrderAggregate;

public record OrderResponse(
    Long orderId,
    Long dispatchId,
    Long deliveryId,

    String orderNumber,
    String deliveryNumber,

    OrderLocationResponse originLocation,
    OrderLocationResponse destinationLocation,
    List<OrderItemResponse> orderItems,

    BigDecimal suggestedFee,
    BigDecimal deliveryFee,

    OrderStatusResponse orderStatus,

    Instant orderOrderedAt,
    Instant dispatchRequestedAt,
    Instant dispatchDispatchedAt,
    Instant deliveryStartedAt,
    Instant deliveryPickedUpAt,
    Instant deliveryDeliveredAt,
    Instant orderCancelledAt
) {

    /**
     * OrderAggregate → OrderResponse 변환
     * 
     * Nested 구조(OrderInfo, DispatchInfo, DeliveryInfo)를 Flat한 Response로 변환
     */
    public static OrderResponse fromOrderAggregate(OrderAggregate aggregate) {
        // Nested 정보 추출
        OrderAggregate.OrderInfo orderInfo = aggregate.getOrderInfo();
        OrderAggregate.DispatchInfo dispatchInfo = aggregate.getDispatchInfo();
        OrderAggregate.DeliveryInfo deliveryInfo = aggregate.getDeliveryInfo();
        
        // IDs
        Long orderId = aggregate.getOrderId();
        Long dispatchId = aggregate.getDispatchId();
        Long deliveryId = aggregate.getDeliveryId();
        
        // Order 정보
        String orderNumber = orderInfo != null ? orderInfo.getOrderNumber() : null;
        String deliveryNumber = deliveryInfo != null ? deliveryInfo.getDeliveryNumber() : null;
        
        // Locations
        OrderLocationResponse originLocation = orderInfo != null && orderInfo.getOriginLocation() != null 
            ? fromOrderLocation(orderInfo.getOriginLocation()) : null;
        OrderLocationResponse destinationLocation = orderInfo != null && orderInfo.getDestinationLocation() != null 
            ? fromOrderLocation(orderInfo.getDestinationLocation()) : null;
        
        // Items
        List<OrderItemResponse> orderItems = orderInfo != null && orderInfo.getItems() != null 
            ? fromOrderItems(orderInfo.getItems()) : null;
        
        // Fees
        BigDecimal suggestedFee = dispatchInfo != null ? dispatchInfo.getSuggestedFee() : null;
        BigDecimal deliveryFee = deliveryInfo != null ? deliveryInfo.getDeliveryFee() : null;
        
        // Status
        OrderStatusResponse orderStatus = determineOrderStatus(orderInfo, dispatchInfo, deliveryInfo);
        
        // Timestamps
        Instant orderOrderedAt = orderInfo != null ? orderInfo.getOrderedAt() : null;
        Instant dispatchRequestedAt = dispatchInfo != null ? dispatchInfo.getRequestedAt() : null;
        Instant dispatchDispatchedAt = dispatchInfo != null ? dispatchInfo.getDispatchedAt() : null;
        Instant deliveryStartedAt = deliveryInfo != null ? deliveryInfo.getDeliveryStartedAt() : null;
        Instant deliveryPickedUpAt = deliveryInfo != null ? deliveryInfo.getDeliveryPickedUpAt() : null;
        Instant deliveryDeliveredAt = deliveryInfo != null ? deliveryInfo.getDeliveryDeliveredAt() : null;
        Instant orderCancelledAt = deliveryInfo != null ? deliveryInfo.getDeliveryCancelledAt() : null;
        
        return new OrderResponse(
            orderId,
            dispatchId,
            deliveryId,
            orderNumber,
            deliveryNumber,
            originLocation,
            destinationLocation,
            orderItems,
            suggestedFee,
            deliveryFee,
            orderStatus,
            orderOrderedAt,
            dispatchRequestedAt,
            dispatchDispatchedAt,
            deliveryStartedAt,
            deliveryPickedUpAt,
            deliveryDeliveredAt,
            orderCancelledAt
        );
    }
    
    /**
     * OrderLocation → OrderLocationResponse 변환
     */
    private static OrderLocationResponse fromOrderLocation(OrderAggregate.OrderLocation location) {
        return new OrderLocationResponse(
            location.getContactName(),
            location.getContactPhoneNumber(),
            location.getLatitude(),
            location.getLongitude(),
            location.getJibunAddress(),
            location.getRoadAddress(),
            location.getDetailAddress()
        );
    }
    
    /**
     * OrderItem List → OrderItemResponse List 변환
     */
    private static List<OrderItemResponse> fromOrderItems(List<OrderAggregate.OrderItem> items) {
        return items.stream()
            .map(OrderResponse::fromOrderItem)
            .collect(Collectors.toList());
    }
    
    /**
     * OrderItem → OrderItemResponse 변환
     */
    private static OrderItemResponse fromOrderItem(OrderAggregate.OrderItem item) {
        return new OrderItemResponse(
            item.getItemName(),
            item.getQuantity(),
            item.getPrice()
        );
    }
    
    /**
     * 현재 주문 상태 결정
     * 
     * OrderInfo, DispatchInfo, DeliveryInfo를 기반으로 가장 최근 상태 반환
     */
    private static OrderStatusResponse determineOrderStatus(
            OrderAggregate.OrderInfo orderInfo,
            OrderAggregate.DispatchInfo dispatchInfo,
            OrderAggregate.DeliveryInfo deliveryInfo) {
        
        // 배송 완료/취소 확인 (최우선)
        if (deliveryInfo != null) {
            if (deliveryInfo.getDeliveryCancelledAt() != null) {
                return OrderStatusResponse.ORDER_CANCELLED;
            }
            if (deliveryInfo.getDeliveryDeliveredAt() != null) {
                return OrderStatusResponse.DELIVERY_DELIVERED;
            }
            if (deliveryInfo.getDeliveryPickedUpAt() != null) {
                return OrderStatusResponse.DELIVERY_PICKED_UP;
            }
            if (deliveryInfo.getDeliveryStartedAt() != null) {
                return OrderStatusResponse.DELIVERY_STARTED;
            }
        }
        
        // 배차 확인
        if (dispatchInfo != null) {
            if (dispatchInfo.getDispatchedAt() != null) {
                return OrderStatusResponse.DISPATCH_DISPATCHED;
            }
            if (dispatchInfo.getRequestedAt() != null) {
                return OrderStatusResponse.DISPATCH_REQUESTED;
            }
        }
        
        // 기본값: 주문 접수
        return OrderStatusResponse.ORDER_ORDERED;
    }
}
