package vroong.laas.readmodel.projection.handler.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.handler.common.DeliveryEventHandler;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.time.Instant;

@Slf4j
@Component
public class DeliveryDeliveredHandler implements DeliveryEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DELIVERY_DELIVERY_DELIVERED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderAggregate existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Handling delivery delivered event: deliveryId={}, agentId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
        
        Instant now = Instant.now();
        
        // 기존 DeliveryInfo 업데이트 (toBuilder 사용)
        OrderAggregate.DeliveryInfo existingDeliveryInfo = existingProjection.getDeliveryInfo();
        if (existingDeliveryInfo == null) {
            log.warn("DeliveryInfo is null for delivered event: deliveryId={}", 
                    deliveryEvent.getDeliveryId());
            throw new IllegalStateException("DeliveryInfo must exist before delivered event");
        }
        
        OrderAggregate.DeliveryInfo updatedDeliveryInfo = existingDeliveryInfo.toBuilder()
                .deliveryStatus(deliveryEvent.getDeliveryStatus())
                .deliveryDeliveredAt(deliveryEvent.getDeliveredAt())
                .build();
        
        // OrderAggregate 업데이트
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .deliveryInfo(updatedDeliveryInfo)
                .updatedAt(now)
                .build();
        
        log.info("Delivery completed: deliveryId={}, agentId={}, orderId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId(), 
                existingProjection.getOrderId());
        
        return updatedProjection;
    }
}