package vroong.laas.readmodel.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.common.model.OrderProjection;

public interface DeliveryEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderProjection handle(OrderProjection existingProjection, DeliveryEvent deliveryEvent);
}