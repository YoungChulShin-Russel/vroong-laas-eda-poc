package vroong.laas.readmodel.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

public interface DeliveryEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderAggregate handle(OrderAggregate existingProjection, DeliveryEvent deliveryEvent);
}