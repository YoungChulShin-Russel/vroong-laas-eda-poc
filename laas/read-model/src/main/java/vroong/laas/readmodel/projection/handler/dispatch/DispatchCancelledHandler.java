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
public class DispatchCancelledHandler implements DispatchEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DISPATCH_DISPATCH_CANCELLED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderAggregate existingProjection, DispatchEvent dispatchEvent) {
        log.debug("Handling dispatch cancelled event: dispatchId={}, orderId={}", 
                dispatchEvent.getDispatchId(), dispatchEvent.getOrderId());
        
        if (!existingProjection.getOrderId().equals(dispatchEvent.getOrderId())) {
            log.warn("Order ID mismatch: projection={}, event={}", 
                    existingProjection.getOrderId(), dispatchEvent.getOrderId());
            throw new IllegalArgumentException("Order ID mismatch in dispatch event");
        }
        
        Instant now = Instant.now();
        
        // 배차가 취소되었으므로 dispatchId와 dispatchInfo를 제거
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .dispatchId(null)  // 배차 ID 제거
                .dispatchInfo(null)  // 배차 정보 제거
                .updatedAt(now)
                .build();
        
        log.info("Dispatch cancelled: orderId={}, cancelledDispatchId={}, cancelledAt={}", 
                updatedProjection.getOrderId(), 
                dispatchEvent.getDispatchId(),  // 취소된 배차 ID
                dispatchEvent.getCancelledAt());
        
        return updatedProjection;
    }
}