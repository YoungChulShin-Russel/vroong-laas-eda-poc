package vroong.laas.readmodel.handler.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.handler.common.DeliveryEventHandler;
import vroong.laas.readmodel.model.event.DeliveryEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

import java.time.Instant;

@Slf4j
@Component
public class DeliveryDeliveredHandler implements DeliveryEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DELIVERY_DELIVERY_DELIVERED.equals(eventType);
    }

    @Override
    public OrderProjection handle(OrderProjection existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Handling delivery delivered event: deliveryId={}, agentId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
        
        Instant now = Instant.now();
        
        OrderProjection updatedProjection = existingProjection.toBuilder()
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