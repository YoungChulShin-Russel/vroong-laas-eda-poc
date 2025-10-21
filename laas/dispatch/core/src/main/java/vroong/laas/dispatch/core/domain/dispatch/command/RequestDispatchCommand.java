package vroong.laas.dispatch.core.domain.dispatch.command;

import java.time.Instant;

public record RequestDispatchCommand(
    Long orderId,
    Instant requestedAt
) {

}
