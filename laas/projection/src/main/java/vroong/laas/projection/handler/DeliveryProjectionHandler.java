package vroong.laas.projection.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.projection.model.event.DeliveryEvent;
import vroong.laas.projection.model.projection.OrderProjection;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryProjectionHandler {

    public OrderProjection updateDeliveryStatus(OrderProjection existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Updating delivery status: deliveryId={}, eventType={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getEventType());
        
        Instant now = Instant.now();
        
        OrderProjection.OrderProjectionBuilder builder = existingProjection.toBuilder()
                .deliveryId(deliveryEvent.getDeliveryId())
                .agentId(deliveryEvent.getAgentId())
                .updatedAt(now);
        
        switch (deliveryEvent.getEventType()) {
            case STARTED:
                builder.deliveryStatus(OrderProjection.DeliveryStatus.STARTED.name())
                       .deliveryStartedAt(Instant.ofEpochMilli(deliveryEvent.getTimestamp()));
                log.info("Delivery started: deliveryId={}, agentId={}", 
                        deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
                break;
                
            case PICKED_UP:
                builder.deliveryStatus(OrderProjection.DeliveryStatus.PICKED_UP.name())
                       .deliveryPickedUpAt(Instant.ofEpochMilli(deliveryEvent.getTimestamp()));
                log.info("Delivery picked up: deliveryId={}, agentId={}", 
                        deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
                break;
                
            case DELIVERED:
                builder.deliveryStatus(OrderProjection.DeliveryStatus.DELIVERED.name())
                       .deliveryDeliveredAt(Instant.ofEpochMilli(deliveryEvent.getTimestamp()));
                log.info("Delivery completed: deliveryId={}, agentId={}", 
                        deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
                break;
                
            default:
                log.warn("Unknown delivery event type: {}", deliveryEvent.getEventType());
                return existingProjection;
        }
        
        OrderProjection updatedProjection = builder.build();
        
        log.debug("Updated delivery projection: orderId={}, deliveryStatus={}", 
                updatedProjection.getOrderId(), updatedProjection.getDeliveryStatus());
        
        return updatedProjection;
    }
}