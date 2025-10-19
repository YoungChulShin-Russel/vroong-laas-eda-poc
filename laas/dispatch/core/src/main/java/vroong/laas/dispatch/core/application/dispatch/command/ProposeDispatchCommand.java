package vroong.laas.dispatch.core.application.dispatch.command;

import java.math.BigDecimal;

public record ProposeDispatchCommand(
    Long agentId,
    Long orderId,
    BigDecimal suggestedFee
) {

}
