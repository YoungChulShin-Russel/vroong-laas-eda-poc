package vroong.laas.projection.model.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;

@Getter
@RequiredArgsConstructor
public class OrderEvent {
    
    private final KafkaEvent<OrderCreatedEventPayload> kafkaEvent;
    
    public Long getOrderId() {
        return kafkaEvent.getPayload().getOrderId();
    }
    
    public String getOrderNumber() {
        return kafkaEvent.getPayload().getOrderNumber();
    }
    
    public String getOrderStatus() {
        return kafkaEvent.getPayload().getOrderStatus();
    }
    
    public OrderCreatedEventPayload.OrderCreatedOrderLocation getOriginLocation() {
        return kafkaEvent.getPayload().getOriginLocation();
    }
    
    public OrderCreatedEventPayload.OrderCreatedOrderLocation getDestinationLocation() {
        return kafkaEvent.getPayload().getDestinationLocation();
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
}