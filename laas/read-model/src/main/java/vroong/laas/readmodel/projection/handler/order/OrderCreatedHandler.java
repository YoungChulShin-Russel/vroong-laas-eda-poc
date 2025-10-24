package vroong.laas.readmodel.projection.handler.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.readmodel.projection.handler.common.OrderEventHandler;
import vroong.laas.readmodel.projection.event.OrderEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

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
    public OrderAggregate handle(OrderEvent orderEvent) {
        log.debug("Handling order created event: orderId={}", orderEvent.getOrderId());
        
        Instant now = Instant.now();
        
        // OrderInfo 구성
        OrderAggregate.OrderInfo orderInfo = OrderAggregate.OrderInfo.builder()
                .orderNumber(orderEvent.getOrderNumber())
                .orderStatus(orderEvent.getOrderStatus())
                .originLocation(convertLocation(orderEvent.getOriginLocation()))
                .destinationLocation(convertLocation(orderEvent.getDestinationLocation()))
                .items(convertItems(orderEvent.getItems()))
                .orderedAt(orderEvent.getOrderedAt())
                .build();
        
        // OrderAggregate 구성 (DispatchInfo, DeliveryInfo는 null)
        OrderAggregate projection = OrderAggregate.builder()
                .orderId(orderEvent.getOrderId())
                .dispatchId(null)
                .deliveryId(null)
                .orderInfo(orderInfo)
                .dispatchInfo(null)  // 아직 배차 정보 없음
                .deliveryInfo(null)  // 아직 배송 정보 없음
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        log.info("Created order projection: orderId={}, orderNumber={}", 
                projection.getOrderId(), projection.getOrderInfo().getOrderNumber());
        
        return projection;
    }
    
    private OrderAggregate.OrderLocation convertLocation(
            vroong.laas.common.event.payload.order.OrderCreatedEventPayload.OrderCreatedOrderLocation location) {
        
        if (location == null) {
            return null;
        }
        
        return OrderAggregate.OrderLocation.builder()
                .contactName(location.getContactName())
                .contactPhoneNumber(location.getContactPhoneNumber())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .jibunAddress(location.getJibunAddress())
                .roadAddress(location.getRoadAddress())
                .detailAddress(location.getDetailAddress())
                .build();
    }
    
    private List<OrderAggregate.OrderItem> convertItems(
            List<vroong.laas.common.event.payload.order.OrderCreatedEventPayload.OrderCreatedOrderItem> items) {
        
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(item -> OrderAggregate.OrderItem.builder()
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
    }
}