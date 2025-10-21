package vroong.laas.dispatch.api.web.dispatch.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import vroong.laas.dispatch.core.domain.dispatch.command.ProposeDispatchCommand;

public record ProposeDispatchRequest(
    @NotNull Long orderId,
    @NotNull Long agentId,
    @NotNull @Min(0) BigDecimal suggestedFee
) {

  public ProposeDispatchCommand toCommand() {
    return new ProposeDispatchCommand(agentId, orderId, suggestedFee);
  }
}
