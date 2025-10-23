package vroong.laas.readmodel.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.model.event.DeliveryEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

public interface DeliveryEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderProjection existingProjection, DeliveryEvent deliveryEvent);
}