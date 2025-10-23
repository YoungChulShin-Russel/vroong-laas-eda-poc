package vroong.laas.readmodel.model.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;

@Getter
@RequiredArgsConstructor
public class OrderEvent {
    
    private final KafkaEvent<? extends KafkaEventPayload> kafkaEvent;
    
    public Long getOrderId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOrderId();
        }
        // 다른 order 이벤트 타입들 추가 가능
        return null;
    }
    
    public String getOrderNumber() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOrderNumber();
        }
        return null;
    }
    
    public String getOrderStatus() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOrderStatus();
        }
        return null;
    }
    
    public OrderCreatedEventPayload.OrderCreatedOrderLocation getOriginLocation() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOriginLocation();
        }
        return null;
    }
    
    public OrderCreatedEventPayload.OrderCreatedOrderLocation getDestinationLocation() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getDestinationLocation();
        }
        return null;
    }
    
    public java.util.List<OrderCreatedEventPayload.OrderCreatedOrderItem> getItems() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getItems();
        }
        return java.util.List.of();
    }
    
    public java.time.Instant getOrderedAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOrderedAt();
        }
        return null;
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
}