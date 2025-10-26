package vroong.laas.readmodel.projection.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.handler.common.OrderEventHandler;
import vroong.laas.readmodel.projection.handler.order.OrderCancelledHandler;
import vroong.laas.readmodel.projection.handler.order.OrderDestinationChangedHandler;
import vroong.laas.readmodel.projection.event.OrderEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProjectionHandler {

    private final List<OrderEventHandler> orderEventHandlers;
    private final OrderCancelledHandler orderCancelledHandler;
    private final OrderDestinationChangedHandler orderDestinationChangedHandler;

    public OrderAggregate handleOrderEvent(OrderEvent orderEvent) {
        log.debug("Finding handler for order event type: {}", orderEvent.getKafkaEvent().getType());
        
        OrderEventHandler handler = orderEventHandlers.stream()
                .filter(h -> h.supports(orderEvent.getKafkaEvent().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No handler found for order event type: " + orderEvent.getKafkaEvent().getType()));
        
        log.debug("Using handler: {} for event type: {}", 
                handler.getClass().getSimpleName(), orderEvent.getKafkaEvent().getType());
        
        return handler.handle(orderEvent);
    }
    
    /**
     * 기존 Order projection을 업데이트하는 메서드 (취소, 목적지 변경)
     */
    public OrderAggregate updateOrderEvent(OrderAggregate existingProjection, OrderEvent orderEvent) {
        log.debug("Updating order event type: {}", orderEvent.getKafkaEvent().getType());
        
        KafkaEventType eventType = orderEvent.getKafkaEvent().getType();
        
        if (KafkaEventType.ORDER_ORDER_CANCELLED.equals(eventType)) {
            return orderCancelledHandler.updateOrderInfo(existingProjection, orderEvent);
        } else if (KafkaEventType.ORDER_ORDER_DESTINATION_CHANGED.equals(eventType)) {
            return orderDestinationChangedHandler.updateOrderInfo(existingProjection, orderEvent);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported order update event type: " + eventType);
        }
    }
}