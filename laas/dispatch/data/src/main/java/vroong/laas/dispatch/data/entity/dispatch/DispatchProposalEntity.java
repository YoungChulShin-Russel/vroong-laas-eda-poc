package vroong.laas.dispatch.data.entity.dispatch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.dispatch.core.enums.dispatch.DispatchProposalStatus;
import vroong.laas.dispatch.data.entity.BaseEntity;

@Entity
@Table(name = "dispatch_proposals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DispatchProposalEntity extends BaseEntity {

    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "suggested_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal suggestedFee;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DispatchProposalStatus status;

    @Column(name = "proposed_at", nullable = false)
    private Instant proposedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Builder
    public DispatchProposalEntity(
        Long dispatchId,
        Long orderId,
        Long agentId,
        BigDecimal suggestedFee,
        DispatchProposalStatus status,
        Instant proposedAt,
        Instant expiresAt,
        Instant respondedAt) {
        this.dispatchId = dispatchId;
        this.orderId = orderId;
        this.agentId = agentId;
        this.suggestedFee = suggestedFee;
        this.status = status;
        this.proposedAt = proposedAt;
        this.expiresAt = expiresAt;
        this.respondedAt = respondedAt;
    }

    public static DispatchProposalEntity register(
        Long dispatchId,
        Long orderId,
        Long agentId,
        BigDecimal suggestedFee,
        Instant expiresAt
    ) {
        return DispatchProposalEntity.builder()
            .dispatchId(dispatchId)
            .orderId(orderId)
            .agentId(agentId)
            .suggestedFee(suggestedFee)
            .status(DispatchProposalStatus.PROPOSED)
            .expiresAt(expiresAt)
            .proposedAt(Instant.now())
            .build();
    }

    public void accept() {
        this.status = DispatchProposalStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    public void decline() {
        this.status = DispatchProposalStatus.DECLINED;
        this.respondedAt = Instant.now();
    }

    public void expire() {
        this.status = DispatchProposalStatus.EXPIRED;
        this.respondedAt = Instant.now();
    }
}