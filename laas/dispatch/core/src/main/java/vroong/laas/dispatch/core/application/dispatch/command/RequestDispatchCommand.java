package vroong.laas.dispatch.core.application.dispatch.command;

import java.time.Instant;
import vroong.laas.dispatch.core.enums.DispatchStatus;

public record RequestDispatchCommand(
    Long orderId,
    DispatchStatus status,
    Instant requestedAt
) {

}
