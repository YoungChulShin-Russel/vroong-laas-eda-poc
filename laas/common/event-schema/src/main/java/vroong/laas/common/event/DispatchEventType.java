package vroong.laas.common.event;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.avro.specific.SpecificRecord;
import vroong.laas.common.event.avro.payload.dispatch.DispatchDispatchedEventPayload;

/**
 * Dispatch 도메인 이벤트 타입
 */
@Getter
public enum DispatchEventType implements EventType {
    
    DISPATCH_DISPATCHED(
        "dispatch.dispatch.dispatched",
        DispatchDispatchedEventPayload.class,
        "dispatch-event"
    );
    // 향후 추가:
    // DISPATCH_ASSIGNED("dispatch.dispatch.assigned", ...),
    // DISPATCH_CANCELLED("dispatch.dispatch.cancelled", ...),
    // ...

    private final String value;
    private final Class<? extends SpecificRecord> payloadClass;
    @Getter(AccessLevel.NONE)
    private final String topicKey;

    DispatchEventType(
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
    public static DispatchEventType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EventType value is null");
        }

        for (DispatchEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown DispatchEventType value: " + value);
    }

    @Override
    public String getTopicName(String environment) {
        return String.format("%s-%s", environment, this.topicKey);
    }
}

