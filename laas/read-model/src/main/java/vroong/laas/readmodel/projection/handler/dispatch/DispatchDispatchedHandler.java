package vroong.laas.readmodel.projection.handler.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.handler.common.DispatchEventHandler;
import vroong.laas.readmodel.projection.event.DispatchEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.time.Instant;

@Slf4j
@Component
public class DispatchDispatchedHandler implements DispatchEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DISPATCH_DISPATCH_DISPATCHED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderAggregate existingProjection, DispatchEvent dispatchEvent) {
        log.debug("Handling dispatch dispatched event: dispatchId={}, orderId={}, agentId={}", 
                dispatchEvent.getDispatchId(), dispatchEvent.getOrderId(), dispatchEvent.getAgentId());
        
        if (!existingProjection.getOrderId().equals(dispatchEvent.getOrderId())) {
            log.warn("Order ID mismatch: projection={}, event={}", 
                    existingProjection.getOrderId(), dispatchEvent.getOrderId());
            throw new IllegalArgumentException("Order ID mismatch in dispatch event");
        }
        
        Instant now = Instant.now();
        
        // DispatchInfo 구성
        OrderAggregate.DispatchInfo dispatchInfo = OrderAggregate.DispatchInfo.builder()
                .agentId(dispatchEvent.getAgentId())
                .suggestedFee(dispatchEvent.getDeliveryFee())
                .dispatchedAt(dispatchEvent.getDispatchedAt())
                .build();
        
        // OrderAggregate 업데이트
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .dispatchId(dispatchEvent.getDispatchId())
                .dispatchInfo(dispatchInfo)
                .updatedAt(now)
                .build();
        
        log.info("Updated dispatch projection: orderId={}, dispatchId={}, agentId={}, suggestedFee={}", 
                updatedProjection.getOrderId(), 
                updatedProjection.getDispatchId(),
                updatedProjection.getDispatchInfo().getAgentId(),
                updatedProjection.getDispatchInfo().getSuggestedFee());
        
        return updatedProjection;
    }
}