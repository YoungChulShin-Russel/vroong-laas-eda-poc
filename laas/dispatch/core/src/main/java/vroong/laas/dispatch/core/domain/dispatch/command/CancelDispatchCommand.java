package vroong.laas.dispatch.core.domain.dispatch.command;

import java.time.Instant;

public record CancelDispatchCommand(
    Long orderId,
    Instant requestedAt
) {

}
