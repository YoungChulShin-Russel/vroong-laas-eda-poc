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
public class OrderCancelledHandler implements OrderEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.ORDER_ORDER_CANCELLED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderEvent orderEvent) {
        log.debug("Handling order cancelled event: orderId={}", orderEvent.getOrderId());
        
        Instant now = Instant.now();
        
        // 기존 OrderInfo 업데이트 (orderStatus를 CANCELLED로 변경)
        // 주문 취소 이벤트의 경우 기존 Order projection이 있어야 함
        throw new UnsupportedOperationException("Order cancellation requires existing projection - use updateOrderInfo method");
    }
    
    /**
     * 기존 Order projection을 업데이트하는 메서드
     * (OrderProjectionHandler에서 호출하도록 수정 필요)
     */
    public OrderAggregate updateOrderInfo(OrderAggregate existingProjection, OrderEvent orderEvent) {
        log.debug("Updating order cancelled: orderId={}", orderEvent.getOrderId());
        
        if (!existingProjection.getOrderId().equals(orderEvent.getOrderId())) {
            log.warn("Order ID mismatch: projection={}, event={}", 
                    existingProjection.getOrderId(), orderEvent.getOrderId());
            throw new IllegalArgumentException("Order ID mismatch in order cancelled event");
        }
        
        Instant now = Instant.now();
        
        // 기존 OrderInfo 업데이트 (orderStatus를 CANCELLED로 변경)
        OrderAggregate.OrderInfo existingOrderInfo = existingProjection.getOrderInfo();
        if (existingOrderInfo == null) {
            log.warn("OrderInfo is null for cancelled event: orderId={}", 
                    orderEvent.getOrderId());
            throw new IllegalStateException("OrderInfo must exist before cancelled event");
        }
        
        OrderAggregate.OrderInfo updatedOrderInfo = existingOrderInfo.toBuilder()
                .orderStatus(orderEvent.getOrderStatus()) // 이벤트에서 주문 상태 가져오기
                .build();
        
        // OrderAggregate 업데이트
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .orderInfo(updatedOrderInfo)
                .updatedAt(now)
                .build();
        
        log.info("Order cancelled: orderId={}, cancelledAt={}", 
                orderEvent.getOrderId(), orderEvent.getCancelledAt());
        
        return updatedProjection;
    }
}