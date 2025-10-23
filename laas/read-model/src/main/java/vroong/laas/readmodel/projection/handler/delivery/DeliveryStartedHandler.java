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
public class DeliveryStartedHandler implements DeliveryEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DELIVERY_DELIVERY_STARTED.equals(eventType);
    }

    @Override
    public OrderInfo handle(OrderInfo existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Handling delivery started event: deliveryId={}, agentId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
        
        Instant now = Instant.now();
        
        OrderInfo updatedProjection = existingProjection.toBuilder()
                .deliveryId(deliveryEvent.getDeliveryId())
                .agentId(deliveryEvent.getAgentId())
                .deliveryStatus(deliveryEvent.getDeliveryStatus())
                .deliveryStartedAt(deliveryEvent.getStartedAt())
                .updatedAt(now)
                .build();
        
        log.info("Delivery started: deliveryId={}, agentId={}, orderId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId(), 
                existingProjection.getOrderId());
        
        return updatedProjection;
    }
}