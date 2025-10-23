package vroong.laas.readmodel.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.model.event.DispatchEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

public interface DispatchEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderProjection existingProjection, DispatchEvent dispatchEvent);
}