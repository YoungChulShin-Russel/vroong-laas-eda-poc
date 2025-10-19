package vroong.laas.delivery.core.domain.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import vroong.laas.delivery.core.domain.BaseEntity;

@Entity
@Table(name = "delivery_dispatch_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryDispatchMapping extends BaseEntity {

  @Column(name = "delivery_id")
  private Long deliveryId;

  @Column(name = "dispatch_id")
  private Long dispatchId;

  private DeliveryDispatchMapping(Long deliveryId, Long dispatchId) {
    this.deliveryId = deliveryId;
    this.dispatchId = dispatchId;
  }

  public static DeliveryDispatchMapping register(Long deliveryId, Long dispatchId) {
    return new DeliveryDispatchMapping(deliveryId, dispatchId);
  }
}
