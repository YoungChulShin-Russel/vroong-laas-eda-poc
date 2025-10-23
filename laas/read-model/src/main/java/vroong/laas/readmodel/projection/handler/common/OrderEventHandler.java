package vroong.laas.readmodel.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.event.OrderEvent;
import vroong.laas.readmodel.common.model.OrderProjection;

public interface OrderEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderEvent orderEvent);
}