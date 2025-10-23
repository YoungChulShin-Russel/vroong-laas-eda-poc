package vroong.laas.readmodel.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.event.DispatchEvent;
import vroong.laas.readmodel.common.model.OrderProjection;

public interface DispatchEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderProjection existingProjection, DispatchEvent dispatchEvent);
}