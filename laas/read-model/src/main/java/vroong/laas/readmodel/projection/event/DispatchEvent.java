package vroong.laas.readmodel.projection.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class DispatchEvent {
    
    private final KafkaEvent<? extends KafkaEventPayload> kafkaEvent;
    
    public Long getDispatchId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getDispatchId();
        }
        return null;
    }
    
    public Long getOrderId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getOrderId();
        }
        return null;
    }
    
    public Long getAgentId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getAgentId();
        }
        return null;
    }
    
    public BigDecimal getDeliveryFee() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getDeliveryFee();
        }
        return null;
    }
    
    public Instant getDispatchedAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getDispatchedAt();
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