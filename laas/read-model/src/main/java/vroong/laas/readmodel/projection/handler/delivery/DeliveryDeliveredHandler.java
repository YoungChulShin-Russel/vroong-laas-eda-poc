package vroong.laas.readmodel.projection.handler.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.handler.common.DeliveryEventHandler;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.common.model.OrderInfo;

import java.time.Instant;

@Slf4j
@Component
public class DeliveryDeliveredHandler implements DeliveryEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DELIVERY_DELIVERY_DELIVERED.equals(eventType);
    }

    @Override
    public OrderInfo handle(OrderInfo existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Handling delivery delivered event: deliveryId={}, agentId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
        
        Instant now = Instant.now();
        
        OrderInfo updatedProjection = existingProjection.toBuilder()
                .deliveryStatus(deliveryEvent.getDeliveryStatus())
                .deliveryDeliveredAt(deliveryEvent.getDeliveredAt())
                .updatedAt(now)
                .build();
        
        log.info("Delivery completed: deliveryId={}, agentId={}, orderId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId(), 
                existingProjection.getOrderId());
        
        return updatedProjection;
    }
}