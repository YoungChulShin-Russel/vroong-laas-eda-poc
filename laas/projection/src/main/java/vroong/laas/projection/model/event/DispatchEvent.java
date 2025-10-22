package vroong.laas.projection.model.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class DispatchEvent {
    
    private final KafkaEvent<DispatchDispatchedEventPayload> kafkaEvent;
    
    public Long getDispatchId() {
        return kafkaEvent.getPayload().getDispatchId();
    }
    
    public Long getOrderId() {
        return kafkaEvent.getPayload().getOrderId();
    }
    
    public Long getAgentId() {
        return kafkaEvent.getPayload().getAgentId();
    }
    
    public BigDecimal getDeliveryFee() {
        return kafkaEvent.getPayload().getDeliveryFee();
    }
    
    public Instant getDispatchedAt() {
        return kafkaEvent.getPayload().getDispatchedAt();
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
}