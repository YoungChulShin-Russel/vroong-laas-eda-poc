package vroong.laas.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.projection.model.event.OrderEvent;
import vroong.laas.projection.model.projection.OrderProjection;

public interface OrderEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderEvent orderEvent);
}