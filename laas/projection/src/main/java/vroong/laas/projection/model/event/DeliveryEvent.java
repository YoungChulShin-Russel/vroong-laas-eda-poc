package vroong.laas.projection.model.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
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
        }
        return null;
    }
    
    public Long getAgentId() {
        KafkaEventPayload payload = kafkaEvent.getPayload();
        if (payload instanceof DeliveryStartedEventPayload startedPayload) {
            return startedPayload.getAgentId();
        } else if (payload instanceof DeliveryPickedUpEventPayload pickedUpPayload) {
            return pickedUpPayload.getAgentId();
        } else if (payload instanceof DeliveryDeliveredEventPayload deliveredPayload) {
            return deliveredPayload.getAgentId();
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
        }
        return DeliveryEventType.UNKNOWN;
    }
    
    public String getEventId() {
        return kafkaEvent.getEventId();
    }
    
    public long getTimestamp() {
        return kafkaEvent.getTimestamp();
    }
    
    public enum DeliveryEventType {
        STARTED, PICKED_UP, DELIVERED, UNKNOWN
    }
}