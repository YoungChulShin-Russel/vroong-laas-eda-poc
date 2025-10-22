package vroong.laas.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.projection.model.event.DispatchEvent;
import vroong.laas.projection.model.projection.OrderProjection;

public interface DispatchEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderProjection existingProjection, DispatchEvent dispatchEvent);
}