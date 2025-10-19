package vroong.laas.delivery.core.domain.delivery;

import lombok.Getter;

/**
 * 배송 상태 Enum
 *
 * <p>배송의 진행 상태를 나타냅니다.
 *
 * <p>상태 전이:
 * - 정상 진행: STARTED → ARRIVED → PICKED_UP → COMPLETED
 * - 취소: STARTED → CANCELLED, ARRIVED → CANCELLED
 */
@Getter
public enum DeliveryStatus {
    
    /** 배송 시작 */
    STARTED("배송 시작", true),

    /** 상점 도착 */
    PICKUP_ARRIVED("상점 도착", false),
    
    /** 픽업 완료 */
    PICKED_UP("픽업 완료", true),
    
    /** 사진 업로드 */
    PHOTO_UPLOAD("배송 사진 업로드", false),
    
    /** 배송 완료 */
    DELIVERED("배송 완료", true),
    
    /** 배송 취소 */
    CANCELLED("배송 취소", true);

    private final String description;
    private final boolean required;

    DeliveryStatus(String description, boolean required) {
        this.description = description;
        this.required = required;
    }
}
