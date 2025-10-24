package vroong.laas.common.event;

import org.apache.avro.specific.SpecificRecord;

/**
 * 모든 이벤트 타입의 공통 인터페이스
 * 
 * 도메인별로 이벤트 타입을 분리하여 관리하기 위한 계약
 */
public interface EventType {
    
    /**
     * 이벤트 타입 문자열
     * @return 이벤트 타입 (예: "order.order.created")
     */
    String getValue();
    
    /**
     * Avro Payload 클래스
     * @return SpecificRecord 클래스
     */
    Class<? extends SpecificRecord> getPayloadClass();
    
    /**
     * 논리적 토픽 키
     * 이 이벤트가 보내질 논리적 토픽을 식별합니다.
     * @param environment 환경 이름 (예: prod, qa1~4, dev1, local)
     *
     * @return 논리적 토픽 키
     */
    String getTopicName(String environment);
}

