package vroong.laas.readmodel.projection.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchCancelledEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchRequestedEventPayload;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class DispatchEvent {
    
    private final KafkaEvent<? extends KafkaEventPayload> kafkaEvent;
    
    public Long getDispatchId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchRequestedEventPayload requestedPayload) {
            return requestedPayload.getDispatchId();
        } else if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getDispatchId();
        } else if (payload instanceof DispatchCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getDispatchId();
        }
        return null;
    }
    
    public Long getOrderId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchRequestedEventPayload requestedPayload) {
            return requestedPayload.getOrderId();
        } else if (payload instanceof DispatchDispatchedEventPayload dispatchedPayload) {
            return dispatchedPayload.getOrderId();
        } else if (payload instanceof DispatchCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getOrderId();
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
    
    public Instant getRequestedAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchRequestedEventPayload requestedPayload) {
            return requestedPayload.getRequestedAt();
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
    
    public Instant getCancelledAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getCancelledAt();
        }
        return null;
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
    
    public DispatchEventType getEventType() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DispatchRequestedEventPayload) {
            return DispatchEventType.REQUESTED;
        } else if (payload instanceof DispatchDispatchedEventPayload) {
            return DispatchEventType.DISPATCHED;
        } else if (payload instanceof DispatchCancelledEventPayload) {
            return DispatchEventType.CANCELLED;
        }
        return DispatchEventType.UNKNOWN;
    }
    
    public enum DispatchEventType {
        REQUESTED, DISPATCHED, CANCELLED, UNKNOWN
    }
}