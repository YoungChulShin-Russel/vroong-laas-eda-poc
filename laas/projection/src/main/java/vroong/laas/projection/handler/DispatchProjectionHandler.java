package vroong.laas.projection.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.projection.model.event.DispatchEvent;
import vroong.laas.projection.model.projection.OrderProjection;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchProjectionHandler {

    public OrderProjection updateDispatchInfo(OrderProjection existingProjection, DispatchEvent dispatchEvent) {
        log.debug("Updating dispatch info: dispatchId={}, orderId={}, agentId={}", 
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