package vroong.laas.dispatch.core.domain.dispatch;

import java.math.BigDecimal;
import java.time.Instant;
import vroong.laas.dispatch.core.enums.dispatch.DispatchStatus;
import vroong.laas.dispatch.data.entity.dispatch.DispatchEntity;

public record Dispatch(
    Long id,
    Long orderId,
    DispatchStatus status,
    Long agentId,
    BigDecimal deliveryFee,
    Instant requestedAt,
    Instant dispatchedAt,
    Instant cancelledAt
) {

  public static Dispatch fromEntity(DispatchEntity entity) {
    return new Dispatch(
        entity.getId(),
        entity.getOrderId(),
        entity.getStatus(),
        entity.getAgentId(),
        entity.getDeliveryFee(),
        entity.getRequestedAt(),
        entity.getDispatchedAt(),
        entity.getCancelledAt());
  }

}
