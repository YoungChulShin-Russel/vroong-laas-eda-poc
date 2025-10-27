package vroong.laas.readmodel.projection.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryCancelledEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryDeliveredEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryPickedUpEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryStartedEventPayload;

@Getter
@RequiredArgsConstructor
public class DeliveryEvent {
    
    private final KafkaEvent<? extends KafkaEventPayload> kafkaEvent;
    
    public Long getDeliveryId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getDeliveryId();
        } else if (payload instanceof DeliveryPickedUpEventPayload pickedUpPayload) {
            return pickedUpPayload.getDeliveryId();
        } else if (payload instanceof DeliveryDeliveredEventPayload deliveredPayload) {
            return deliveredPayload.getDeliveryId();
        } else if (payload instanceof DeliveryCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getDeliveryId();
        }
        return null;
    }
    
    public Long getOrderId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getOrderId();
        } else if (payload instanceof DeliveryPickedUpEventPayload pickedUpPayload) {
            return pickedUpPayload.getOrderId();
        } else if (payload instanceof DeliveryDeliveredEventPayload deliveredPayload) {
            return deliveredPayload.getOrderId();
        } else if (payload instanceof DeliveryCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getOrderId();
        }
        return null;
    }
    
    public String getDeliveryStatus() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getDeliveryStatus();
        } else if (payload instanceof DeliveryPickedUpEventPayload pickedUpPayload) {
            return pickedUpPayload.getDeliveryStatus();
        } else if (payload instanceof DeliveryDeliveredEventPayload deliveredPayload) {
            return deliveredPayload.getDeliveryStatus();
        } else if (payload instanceof DeliveryCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getDeliveryStatus();
        }
        return null;
    }
    
    public java.time.Instant getStartedAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getStartedAt();
        }
        return null;
    }
    
    public java.time.Instant getPickedUpAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryPickedUpEventPayload pickedUpPayload) {
            return pickedUpPayload.getPickedUpAt();
        }
        return null;
    }
    
    public java.time.Instant getDeliveredAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryDeliveredEventPayload deliveredPayload) {
            return deliveredPayload.getDeliveredAt();
        }
        return null;
    }
    
    public Long getAgentId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getAgentId();
        }
        // DeliveryPickedUpEventPayload와 DeliveryDeliveredEventPayload에는 agentId가 없음
        return null;
    }
    
    public String getAgentName() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getAgentName();
        }
        return null;
    }
    
    public String getAgentNumber() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getAgentNumber();
        }
        return null;
    }
    
    public String getAgentPhoneNumber() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getAgentPhoneNumber();
        }
        return null;
    }
    
    public String getDeliveryNumber() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getDeliveryNumber();
        }
        return null;
    }
    
    public java.math.BigDecimal getDeliveryFee() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getDeliveryFee();
        }
        return null;
    }
    
    public DeliveryEventType getEventType() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload) {
            return DeliveryEventType.STARTED;
        } else if (payload instanceof DeliveryPickedUpEventPayload) {
            return DeliveryEventType.PICKED_UP;
        } else if (payload instanceof DeliveryDeliveredEventPayload) {
            return DeliveryEventType.DELIVERED;
        } else if (payload instanceof DeliveryCancelledEventPayload) {
            return DeliveryEventType.CANCELLED;
        }
        return DeliveryEventType.UNKNOWN;
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
    
    public java.time.Instant getCancelledAt() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getCancelledAt();
        }
        return null;
    }
    
    public String getCancelReason() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryCancelledEventPayload cancelledPayload) {
            return cancelledPayload.getReason();
        }
        return null;
    }
    
    public enum DeliveryEventType {
        STARTED, PICKED_UP, DELIVERED, CANCELLED, UNKNOWN
    }
}