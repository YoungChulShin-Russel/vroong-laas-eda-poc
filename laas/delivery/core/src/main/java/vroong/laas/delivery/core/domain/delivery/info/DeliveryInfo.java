package vroong.laas.delivery.core.domain.delivery.info;

import java.math.BigDecimal;
import vroong.laas.delivery.core.domain.delivery.Delivery;
import vroong.laas.delivery.core.domain.delivery.DeliveryNumber;
import vroong.laas.delivery.core.domain.delivery.DeliveryStatus;

public record DeliveryInfo(
    Long deliveryId,
    DeliveryNumber deliveryNumber,
    Long orderId,
    Long agentId,
    BigDecimal deliveryFee,
    DeliveryStatus status
) {

  public static DeliveryInfo fromEntity(Delivery entity) {
    return new DeliveryInfo(
        entity.getId(),
        entity.getDeliveryNumber(),
        entity.getOrderId(),
        entity.getAgentId(),
        entity.getDeliveryFee(),
        entity.getStatus()
    );
  }

}
