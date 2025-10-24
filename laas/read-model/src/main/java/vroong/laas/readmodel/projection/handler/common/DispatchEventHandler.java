package vroong.laas.readmodel.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.event.DispatchEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

public interface DispatchEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderAggregate handle(OrderAggregate existingProjection, DispatchEvent dispatchEvent);
}