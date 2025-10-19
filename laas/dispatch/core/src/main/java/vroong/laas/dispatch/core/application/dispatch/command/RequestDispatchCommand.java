package vroong.laas.dispatch.core.application.dispatch.command;

import java.time.Instant;

public record RequestDispatchCommand(
    Long orderId,
    Instant requestedAt
) {

}
