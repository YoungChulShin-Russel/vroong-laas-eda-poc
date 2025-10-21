package vroong.laas.dispatch.data.entity.dispatch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.dispatch.core.enums.DispatchStatus;
import vroong.laas.dispatch.data.entity.ConcurrentEntity;

@Getter
@Entity
@Table(name = "dispatches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DispatchEntity extends ConcurrentEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DispatchStatus status;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "requested_at")
    private Instant requestedAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Builder
    public DispatchEntity(
            Long orderId,
            DispatchStatus status,
            Long agentId,
            Instant requestedAt,
            Instant dispatchedAt,
            Instant cancelledAt) {
        this.orderId = orderId;
        this.status = status;
        this.agentId = agentId;
        this.requestedAt = requestedAt;
        this.dispatchedAt = dispatchedAt;
        this.cancelledAt = cancelledAt;
    }

    public static DispatchEntity register(
        Long orderId,
        Instant requestedAt
    ) {
        return DispatchEntity.builder()
            .orderId(orderId)
            .status(DispatchStatus.REQUESTED)
            .requestedAt(requestedAt)
            .build();
    }

    public void dispatch(Long agentId, Instant dispatchedAt) {
        this.agentId = agentId;
        this.dispatchedAt = dispatchedAt;
        this.status = DispatchStatus.DISPATCHED;
    }
}