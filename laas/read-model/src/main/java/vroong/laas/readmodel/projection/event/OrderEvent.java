package vroong.laas.readmodel.projection.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.order.OrderCancelledEventPayload;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;
import vroong.laas.common.event.payload.order.OrderDestinationChangedEventPayload;
import vroong.laas.common.event.payload.order.OrderLocationEventDto;
import vroong.laas.common.event.payload.order.OrderItemEventDto;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderEvent {
    
    private final KafkaEvent<? extends KafkaEventPayload> kafkaEvent;
    
    public Long getOrderId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOrderId();
        } else if (payload instanceof OrderCancelledEventPayload orderCancelledPayload) {
            return orderCancelledPayload.getOrderId();
        } else if (payload instanceof OrderDestinationChangedEventPayload destinationChangedPayload) {
            return destinationChangedPayload.getOrderId();
        }
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
        } else if (payload instanceof OrderCancelledEventPayload orderCancelledPayload) {
            return orderCancelledPayload.getOrderStatus();
        }
        return null;
    }
    
    public OrderLocationEventDto getOriginLocation() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOriginLocation();
        }
        return null;
    }
    
    public OrderLocationEventDto getDestinationLocation() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getDestinationLocation();
        } else if (payload instanceof OrderDestinationChangedEventPayload destinationChangedPayload) {
            return destinationChangedPayload.getDestinationLocation();
        }
        return null;
    }
    
    public List<OrderItemEventDto> getItems() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getItems();
        }
        return List.of();
    }
    
    public java.time.Instant getOrderedAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload orderCreatedPayload) {
            return orderCreatedPayload.getOrderedAt();
        }
        return null;
    }
    
    public java.time.Instant getCancelledAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCancelledEventPayload orderCancelledPayload) {
            return orderCancelledPayload.getCancelledAt();
        }
        return null;
    }
    
    public java.time.Instant getDestinationChangedAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderDestinationChangedEventPayload destinationChangedPayload) {
            return destinationChangedPayload.getChangedAt();
        }
        return null;
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
    
    public OrderEventType getEventType() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof OrderCreatedEventPayload) {
            return OrderEventType.CREATED;
        } else if (payload instanceof OrderCancelledEventPayload) {
            return OrderEventType.CANCELLED;
        } else if (payload instanceof OrderDestinationChangedEventPayload) {
            return OrderEventType.DESTINATION_CHANGED;
        }
        return OrderEventType.UNKNOWN;
    }
    
    public enum OrderEventType {
        CREATED, CANCELLED, DESTINATION_CHANGED, UNKNOWN
    }
}