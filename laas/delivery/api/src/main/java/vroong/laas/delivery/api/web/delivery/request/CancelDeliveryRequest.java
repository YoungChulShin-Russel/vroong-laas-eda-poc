package vroong.laas.delivery.api.web.delivery.request;

import jakarta.validation.constraints.NotNull;
import vroong.laas.delivery.core.domain.delivery.command.GiveUpDeliveryCommand;

public record CancelDeliveryRequest(
    @NotNull Long agentId,
    @NotNull String reason
) {

  public GiveUpDeliveryCommand toCommand(Long deliveryId) {
    return new GiveUpDeliveryCommand(agentId, deliveryId, reason);
  }

}
