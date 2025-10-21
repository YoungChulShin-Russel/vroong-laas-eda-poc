package vroong.laas.dispatch.core.domain.dispatch.command;

import java.math.BigDecimal;

public record ProposeDispatchCommand(
    Long agentId,
    Long orderId,
    BigDecimal suggestedFee
) {

}
