package vroong.laas.readmodel.handler.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.handler.common.DispatchEventHandler;
import vroong.laas.readmodel.model.event.DispatchEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

import java.time.Instant;

@Slf4j
@Component
public class DispatchDispatchedHandler implements DispatchEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DISPATCH_DISPATCH_DISPATCHED.equals(eventType);
    }

    @Override
    public OrderProjection handle(OrderProjection existingProjection, DispatchEvent dispatchEvent) {
        log.debug("Handling dispatch dispatched event: dispatchId={}, orderId={}, agentId={}", 
                dispatchEvent.getDispatchId(), dispatchEvent.getOrderId(), dispatchEvent.getAgentId());
        
        if (!existingProjection.getOrderId().equals(dispatchEvent.getOrderId())) {
            log.warn("Order ID mismatch: projection={}, event={}", 
                    existingProjection.getOrderId(), dispatchEvent.getOrderId());
            throw new IllegalArgumentException("Order ID mismatch in dispatch event");
        }
        
        Instant now = Instant.now();
        
        OrderProjection updatedProjection = existingProjection.toBuilder()
                .dispatchId(dispatchEvent.getDispatchId())
                .agentId(dispatchEvent.getAgentId())
                .deliveryFee(dispatchEvent.getDeliveryFee())
                .dispatchedAt(dispatchEvent.getDispatchedAt())
                .updatedAt(now)
                .build();
        
        log.info("Updated dispatch projection: orderId={}, dispatchId={}, agentId={}, deliveryFee={}", 
                updatedProjection.getOrderId(), 
                updatedProjection.getDispatchId(),
                updatedProjection.getAgentId(),
                updatedProjection.getDeliveryFee());
        
        return updatedProjection;
    }
}