package vroong.laas.dispatch.core.domain.dispatch;

import java.math.BigDecimal;
import java.time.Instant;
import vroong.laas.dispatch.core.enums.DispatchProposalStatus;
import vroong.laas.dispatch.data.entity.dispatch.DispatchProposalEntity;

public record DispatchProposal(
    Long proposalId,
    Long dispatchId,
    Long orderId,
    Long agentId,
    BigDecimal suggestedFee,
    DispatchProposalStatus status,
    Instant proposedAt,
    Instant expiresAt,
    Instant respondedAt
) {

  public static DispatchProposal fromEntity(DispatchProposalEntity entity) {
    return new DispatchProposal(
        entity.getId(),
        entity.getDispatchId(),
        entity.getOrderId(),
        entity.getAgentId(),
        entity.getSuggestedFee(),
        entity.getStatus(),
        entity.getProposedAt(),
        entity.getExpiresAt(),
        entity.getRespondedAt());
  }

}
