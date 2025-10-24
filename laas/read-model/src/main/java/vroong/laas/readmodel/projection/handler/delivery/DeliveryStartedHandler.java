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
public class DeliveryStartedHandler implements DeliveryEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DELIVERY_DELIVERY_STARTED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderAggregate existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Handling delivery started event: deliveryId={}, agentId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
        
        Instant now = Instant.now();
        
        // 기존 DeliveryInfo가 있으면 업데이트, 없으면 새로 생성
        OrderAggregate.DeliveryInfo deliveryInfo = OrderAggregate.DeliveryInfo.builder()
                .deliveryNumber(null)  // TODO: 이벤트에서 받아오도록 수정 필요
                .agentId(deliveryEvent.getAgentId())
                .deliveryFee(null)  // TODO: 이벤트에서 받아오도록 수정 필요
                .deliveryStatus(deliveryEvent.getDeliveryStatus())
                .deliveryStartedAt(deliveryEvent.getStartedAt())
                .deliveryPickedUpAt(null)
                .deliveryDeliveredAt(null)
                .deliveryCancelledAt(null)
                .build();
        
        // OrderAggregate 업데이트
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .deliveryId(deliveryEvent.getDeliveryId())
                .deliveryInfo(deliveryInfo)
                .updatedAt(now)
                .build();
        
        log.info("Delivery started: deliveryId={}, agentId={}, orderId={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId(), 
                existingProjection.getOrderId());
        
        return updatedProjection;
    }
}