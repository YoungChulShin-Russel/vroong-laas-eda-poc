package vroong.laas.common.event;

import java.util.stream.Stream;

/**
 * 모든 이벤트 타입을 조회하기 위한 유틸리티 클래스
 * 
 * 도메인별로 분리된 EventType을 통합 조회할 때 사용
 */
public final class EventTypes {

    private EventTypes() {
        // 유틸리티 클래스
    }

    /**
     * 이벤트 타입 문자열로부터 EventType 조회
     * 
     * @param value 이벤트 타입 문자열 (예: "order.order.created")
     * @return 해당하는 EventType
     * @throws IllegalArgumentException 매칭되는 이벤트 타입이 없는 경우
     */
    public static EventType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EventType value is null");
        }

        // 도메인 prefix로 빠르게 찾기
        if (value.startsWith("order.")) {
            return OrderEventType.from(value);
        } else if (value.startsWith("delivery.")) {
            return DeliveryEventType.from(value);
        } else if (value.startsWith("dispatch.")) {
            return DispatchEventType.from(value);
        }

        throw new IllegalArgumentException("Unknown EventType value: " + value);
    }

    /**
     * 모든 이벤트 타입 조회
     * 
     * @return 모든 도메인의 이벤트 타입 스트림
     */
    public static Stream<EventType> all() {
        return Stream.of(
            Stream.of(OrderEventType.values()),
            Stream.of(DeliveryEventType.values()),
            Stream.of(DispatchEventType.values())
        ).flatMap(s -> s);
    }

    /**
     * 특정 도메인의 이벤트 타입 조회
     * 
     * @param domainHint 도메인 이름
     * @return 해당 도메인의 이벤트 타입 배열
     */
    public static EventType[] getByDomain(String domainHint) {
        return switch(domainHint) {
            case "order" -> OrderEventType.values();
            case "delivery" -> DeliveryEventType.values();
            case "dispatch" -> DispatchEventType.values();
            default -> throw new IllegalArgumentException("Unknown domain: " + domainHint);
        };
    }
}

