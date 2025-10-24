package vroong.laas.readmodel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Order Aggregate (Read Model)
 * 
 * 역할: Order, Dispatch, Delivery 도메인의 이벤트를 통합한 Projection Model
 * 
 * 구조:
 * - Root Level: 각 도메인의 ID (orderId, dispatchId, deliveryId)
 * - Nested: 각 도메인별 상세 정보 (OrderInfo, DispatchInfo, DeliveryInfo)
 * 
 * 이벤트 소스:
 * - OrderCreated → OrderInfo 업데이트
 * - DispatchDispatched → DispatchInfo 업데이트
 * - DeliveryStarted/PickedUp/Delivered → DeliveryInfo 업데이트
 * 
 * 저장소:
 * - MongoDB: 영구 저장소
 * - Redis: 캐시 레이어 (TTL 기반)
 * 
 * 설계 원칙:
 * - 도메인별 명확한 분리 (Order, Dispatch, Delivery)
 * - Nullable 필드: dispatchInfo, deliveryInfo (아직 발생하지 않은 이벤트)
 * - Immutable: toBuilder()를 통한 부분 업데이트 지원
 */
@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderAggregate {
    
    // ========================================
    // Root Level IDs
    // ========================================
    
    /**
     * Order ID (필수)
     */
    private final Long orderId;
    
    /**
     * Dispatch ID (옵셔널)
     * 배차가 완료되면 설정됨
     */
    private final Long dispatchId;
    
    /**
     * Delivery ID (옵셔널)
     * 배송이 시작되면 설정됨
     */
    private final Long deliveryId;

    // ========================================
    // Domain Nested Info
    // ========================================
    
    /**
     * Order 도메인 정보 (필수)
     */
    private final OrderInfo orderInfo;
    
    /**
     * Dispatch 도메인 정보 (옵셔널)
     */
    private final DispatchInfo dispatchInfo;
    
    /**
     * Delivery 도메인 정보 (옵셔널)
     */
    private final DeliveryInfo deliveryInfo;

    // ========================================
    // Projection 메타데이터
    // ========================================
    
    /**
     * Projection 최초 생성 시각 (필수)
     */
    private final Instant createdAt;
    
    /**
     * Projection 마지막 업데이트 시각 (필수)
     */
    private final Instant updatedAt;

    /**
     * Order 도메인 정보
     * 
     * 소스: OrderCreated 이벤트
     */
    @Getter
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderInfo {
        private final String orderNumber;
        private final String orderStatus;
        private final OrderLocation originLocation;
        private final OrderLocation destinationLocation;
        private final List<OrderItem> items;
        private final Instant orderedAt;
    }

    /**
     * Dispatch 도메인 정보
     * 
     * 소스: DispatchDispatched 이벤트
     * 참고: suggestedFee는 이벤트의 deliveryFee를 받아옴
     */
    @Getter
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DispatchInfo {
        private final Long agentId;
        private final BigDecimal suggestedFee; // 배차 시점에 제시된 배송비
        private final Instant requestedAt;
        private final Instant dispatchedAt;
    }

    /**
     * Delivery 도메인 정보
     * 
     * 소스: DeliveryStarted, DeliveryPickedUp, DeliveryDelivered 이벤트
     * 참고: 
     * - deliveryNumber, deliveryFee: DeliveryStarted 이벤트에서 설정 (TODO: 이벤트에 추가 필요)
     * - agentId: Dispatch의 agentId와 동일하지만 Delivery 이벤트에도 포함
     */
    @Getter
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryInfo {
        private final String deliveryNumber;
        private final Long agentId;
        private final BigDecimal deliveryFee; // 실제 배송비 (suggestedFee와 다를 수 있음)
        private final String deliveryStatus;

        private final Instant deliveryStartedAt;
        private final Instant deliveryPickedUpAt;
        private final Instant deliveryDeliveredAt;
        private final Instant deliveryCancelledAt;
    }
    
    /**
     * 주소 정보 (출발지/목적지)
     */
    @Getter
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderLocation {
        private final String contactName;
        private final String contactPhoneNumber;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String jibunAddress;
        private final String roadAddress;
        private final String detailAddress;
    }

    /**
     * 주문 아이템 정보
     */
    @Getter
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderItem {
        private final String itemName;
        private final Integer quantity;
        private final BigDecimal price;
    }
    
}