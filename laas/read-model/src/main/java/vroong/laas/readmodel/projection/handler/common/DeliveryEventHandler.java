package vroong.laas.readmodel.projection.handler.common;

import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.common.model.OrderInfo;

public interface DeliveryEventHandler {
    
    boolean supports(KafkaEventType eventType);
    
    OrderInfo handle(OrderInfo existingProjection, DeliveryEvent deliveryEvent);
}