package vroong.laas.readmodel.handler.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.handler.common.OrderEventHandler;
import vroong.laas.readmodel.model.event.OrderEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderCreatedHandler implements OrderEventHandler {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return KafkaEventType.ORDER_ORDER_CREATED.equals(eventType);
    }

    @Override
    public OrderProjection handle(OrderEvent orderEvent) {
        log.debug("Handling order created event: orderId={}", orderEvent.getOrderId());
        
        Instant now = Instant.now();
        
        OrderProjection projection = OrderProjection.builder()
                .orderId(orderEvent.getOrderId())
                .orderNumber(orderEvent.getOrderNumber())
                .orderStatus(orderEvent.getOrderStatus())
                .originLocation(convertLocation(orderEvent.getOriginLocation()))
                .destinationLocation(convertLocation(orderEvent.getDestinationLocation()))
                .items(convertItems(orderEvent.getItems()))
                .orderedAt(orderEvent.getOrderedAt())
                
                // 초기값 설정 (아직 배차/배송 정보 없음)
                .dispatchId(null)
                .agentId(null)
                .deliveryFee(null)
                .dispatchedAt(null)
                
                .deliveryId(null)
                .deliveryStatus(null)
                .deliveryStartedAt(null)
                .deliveryPickedUpAt(null)
                .deliveryDeliveredAt(null)
                .deliveryCancelledAt(null)
                
                // 메타데이터
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        log.info("Created order projection: orderId={}, orderNumber={}", 
                projection.getOrderId(), projection.getOrderNumber());
        
        return projection;
    }
    
    private OrderProjection.OrderLocation convertLocation(
            vroong.laas.common.event.payload.order.OrderCreatedEventPayload.OrderCreatedOrderLocation location) {
        
        if (location == null) {
            return null;
        }
        
        return OrderProjection.OrderLocation.builder()
                .contactName(location.getContactName())
                .contactPhoneNumber(location.getContactPhoneNumber())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .jibunAddress(location.getJibunAddress())
                .roadAddress(location.getRoadAddress())
                .detailAddress(location.getDetailAddress())
                .build();
    }
    
    private List<OrderProjection.OrderItem> convertItems(
            List<vroong.laas.common.event.payload.order.OrderCreatedEventPayload.OrderCreatedOrderItem> items) {
        
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(item -> OrderProjection.OrderItem.builder()
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
    }
}