package vroong.laas.delivery.api.web.delivery.request;

import jakarta.validation.constraints.NotNull;
import vroong.laas.delivery.core.domain.delivery.command.CancelDeliveryCommand;

public record CancelDeliveryRequest(
    @NotNull Long agentId,
    @NotNull String reason
) {

  public CancelDeliveryCommand toCommand(Long deliveryId) {
    return new CancelDeliveryCommand(deliveryId, agentId, reason);
  }

}
