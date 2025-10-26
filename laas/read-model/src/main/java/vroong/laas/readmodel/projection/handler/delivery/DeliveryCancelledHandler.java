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
public class DeliveryCancelledHandler implements DeliveryEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.DELIVERY_DELIVERY_CANCELLED.equals(eventType);
    }

    @Override
    public OrderAggregate handle(OrderAggregate existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Handling delivery cancelled event: deliveryId={}, reason={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getCancelReason());
        
        Instant now = Instant.now();
        
        // 배송이 취소되었으므로 deliveryId와 deliveryInfo를 제거
        OrderAggregate updatedProjection = existingProjection.toBuilder()
                .deliveryId(null)  // 배송 ID 제거
                .deliveryInfo(null)  // 배송 정보 제거
                .updatedAt(now)
                .build();
        
        log.info("Delivery cancelled: cancelledDeliveryId={}, reason={}, orderId={}", 
                deliveryEvent.getDeliveryId(),  // 취소된 배송 ID
                deliveryEvent.getCancelReason(), 
                updatedProjection.getOrderId());
        
        return updatedProjection;
    }
}