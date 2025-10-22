package vroong.laas.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.projection.model.event.DeliveryEvent;
import vroong.laas.projection.model.projection.OrderProjection;

public interface DeliveryEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderProjection existingProjection, DeliveryEvent deliveryEvent);
}