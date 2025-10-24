package vroong.laas.common.event;

import lombok.Getter;
import org.apache.avro.specific.SpecificRecord;
import vroong.laas.common.event.avro.payload.order.OrderCreatedEventPayload;

/**
 * Order 도메인 이벤트 타입
 */
@Getter
public enum OrderEventType implements EventType {
    
    ORDER_CREATED(
        "order.order.created",
        OrderCreatedEventPayload.class,
        "order-event"
    );
    // 향후 추가 예시:
    // ORDER_UPDATED("order.order.updated", OrderUpdatedEventPayload.class, TopicKey.ORDER_MAIN),
    // ORDER_CANCELLED("order.order.cancelled", OrderCancelledEventPayload.class, TopicKey.ORDER_MAIN),
    // PAYMENT_PROCESSED("order.payment.processed", PaymentProcessedEventPayload.class, TopicKey.ORDER_PAYMENT),
    // ...

    private final String value;
    private final Class<? extends SpecificRecord> payloadClass;
    private final String topicKey;

    OrderEventType(
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
    public static OrderEventType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EventType value is null");
        }

        for (OrderEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown OrderEventType value: " + value);
    }

    @Override
    public String getTopicName(String environment) {
        return String.format("%s-%s", environment, this.topicKey);
    }
}

