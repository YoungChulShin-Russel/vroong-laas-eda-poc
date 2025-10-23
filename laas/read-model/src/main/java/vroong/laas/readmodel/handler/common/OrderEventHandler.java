package vroong.laas.readmodel.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.model.event.OrderEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

public interface OrderEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderEvent orderEvent);
}