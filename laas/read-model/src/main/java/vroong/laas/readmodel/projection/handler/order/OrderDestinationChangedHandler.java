package vroong.laas.readmodel.projection.handler.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.handler.common.OrderEventHandler;
import vroong.laas.readmodel.projection.event.OrderEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.time.Instant;

@Slf4j
@Component
public class OrderDestinationChangedHandler implements OrderEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.ORDER_ORDER_DESTINATION_CHANGED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderEvent orderEvent) {
        log.debug("Handling order destination changed event: orderId={}", orderEvent.getOrderId());
        
        // 목적지 변경 이벤트의 경우 기존 Order projection이 있어야 함
        throw new UnsupportedOperationException("Order destination change requires existing projection - use updateOrderInfo method");
    }
    
    /**
     * 기존 Order projection을 업데이트하는 메서드
     * (OrderProjectionHandler에서 호출하도록 수정 필요)
     */
    public OrderAggregate updateOrderInfo(OrderAggregate existingProjection, OrderEvent orderEvent) {
        log.debug("Updating order destination: orderId={}", orderEvent.getOrderId());
        
        if (!existingProjection.getOrderId().equals(orderEvent.getOrderId())) {
            log.warn("Order ID mismatch: projection={}, event={}", 
                    existingProjection.getOrderId(), orderEvent.getOrderId());
            throw new IllegalArgumentException("Order ID mismatch in order destination changed event");
        }
        
        Instant now = Instant.now();
        
        // 기존 OrderInfo 업데이트 (destinationLocation 변경)
        OrderAggregate.OrderInfo existingOrderInfo = existingProjection.getOrderInfo();
        if (existingOrderInfo == null) {
            log.warn("OrderInfo is null for destination changed event: orderId={}", 
                    orderEvent.getOrderId());
            throw new IllegalStateException("OrderInfo must exist before destination changed event");
        }
        
        OrderAggregate.OrderInfo updatedOrderInfo = existingOrderInfo.toBuilder()
                .destinationLocation(convertLocation(orderEvent.getDestinationLocation()))
                .build();
        
        // OrderAggregate 업데이트
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .orderInfo(updatedOrderInfo)
                .updatedAt(now)
                .build();
        
        log.info("Order destination changed: orderId={}, changedAt={}", 
                orderEvent.getOrderId(), orderEvent.getDestinationChangedAt());
        
        return updatedProjection;
    }
    
    private OrderAggregate.OrderLocation convertLocation(
            vroong.laas.common.event.payload.order.OrderLocationEventDto location) {
        
        if (location == null) {
            return null;
        }
        
        return OrderAggregate.OrderLocation.builder()
                .contactName(location.getContactName())
                .contactPhoneNumber(location.getContactPhoneNumber())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .jibunAddress(location.getJibunAddress())
                .roadAddress(location.getRoadAddress())
                .detailAddress(location.getDetailAddress())
                .build();
    }
}