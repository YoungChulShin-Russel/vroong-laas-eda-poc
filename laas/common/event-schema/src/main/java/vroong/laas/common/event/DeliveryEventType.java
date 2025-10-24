package vroong.laas.common.event;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.avro.specific.SpecificRecord;
import vroong.laas.common.event.avro.payload.delivery.DeliveryDeliveredEventPayload;
import vroong.laas.common.event.avro.payload.delivery.DeliveryPickedUpEventPayload;
import vroong.laas.common.event.avro.payload.delivery.DeliveryStartedEventPayload;

/**
 * Delivery 도메인 이벤트 타입
 */
@Getter
public enum DeliveryEventType implements EventType {
    
    DELIVERY_STARTED(
        "delivery.delivery.started",
        DeliveryStartedEventPayload.class,
        "delivery-event"
    ),
    
    DELIVERY_PICKED_UP(
        "delivery.delivery.picked-up",
        DeliveryPickedUpEventPayload.class,
        "delivery-event"
    ),
    
    DELIVERY_DELIVERED(
        "delivery.delivery.delivered",
        DeliveryDeliveredEventPayload.class,
        "delivery-event"
    );
    // 향후 추가:
    // DELIVERY_CANCELLED("delivery.delivery.cancelled", ...),
    // DELIVERY_FAILED("delivery.delivery.failed", ...),
    // ...

    private final String value;
    private final Class<? extends SpecificRecord> payloadClass;
    @Getter(AccessLevel.NONE)
    private final String topicKey;

    DeliveryEventType(
        String value, 
        Class<? extends SpecificRecord> payloadClass,
        String topicKey
    ) {
        this.value = value;
        this.payloadClass = payloadClass;
        this.topicKey = topicKey;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * 이벤트 타입 문자열로부터 enum 조회
     */
    public static DeliveryEventType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EventType value is null");
        }

        for (DeliveryEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown DeliveryEventType value: " + value);
    }

    @Override
    public String getTopicName(String environment) {
        return String.format("%s-%s", environment, this.topicKey);
    }
}

