package vroong.laas.delivery.api.web.delivery.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;

public record RegisterDeliveryRequest(
    @NotNull Long dispatchId,
    @NotNull Long orderId,
    @NotNull Long agentId,
    @NotNull BigDecimal deliveryFee
) {

  public RegisterDeliveryCommand toCommand() {
    return new RegisterDeliveryCommand(
        this.dispatchId,
        this.orderId,
        this.agentId,
        this.deliveryFee);
  }

}
