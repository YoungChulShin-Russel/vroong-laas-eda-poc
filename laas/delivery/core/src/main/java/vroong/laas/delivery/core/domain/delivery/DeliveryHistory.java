package vroong.laas.delivery.core.domain.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.delivery.core.domain.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "delivery_histories")
public class DeliveryHistory extends BaseEntity {

  @Column(name = "delivery_id")
  private Long deliveryId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private DeliveryStatus status;

  @Column(name = "action")
  @Enumerated(EnumType.STRING)
  private DeliveryAction action;

  @Column(name = "reason")
  private String reason;

  @Column(name = "registered_at")
  private Instant registeredAt;

  public static DeliveryHistory appendNormal(Delivery delivery) {
    return appendNormal(delivery, null);
  }

  public static DeliveryHistory appendNormal(Delivery delivery, String reason) {
    return new DeliveryHistory(
        delivery.getId(),
        delivery.getStatus(),
        DeliveryAction.NORMAL,
        reason,
        Instant.now());
  }

  public static DeliveryHistory appendCancel(
      Delivery delivery,
      DeliveryStatus status
  ) {
    return appendCancel(delivery, status, null);
  }

  public static DeliveryHistory appendCancel(
      Delivery delivery,
      DeliveryStatus status,
      String reason
  ) {
    return new DeliveryHistory(
        delivery.getId(),
        status,
        DeliveryAction.CANCEL,
        reason,
        Instant.now());
  }


}
