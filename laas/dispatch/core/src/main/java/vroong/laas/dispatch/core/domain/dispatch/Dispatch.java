package vroong.laas.dispatch.core.domain.dispatch;

import java.time.Instant;
import vroong.laas.dispatch.core.enums.DispatchStatus;
import vroong.laas.dispatch.data.entity.dispatch.DispatchEntity;

public record Dispatch(
    Long dispatchId,
    Long orderId,
    DispatchStatus status,
    Long agentId,
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
        entity.getRequestedAt(),
        entity.getDispatchedAt(),
        entity.getCancelledAt());
  }

}
