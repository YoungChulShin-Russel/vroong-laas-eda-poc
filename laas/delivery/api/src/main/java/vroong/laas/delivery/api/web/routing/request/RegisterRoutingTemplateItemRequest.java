package vroong.laas.delivery.api.web.routing.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import vroong.laas.delivery.core.domain.delivery.DeliveryStatus;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateCommand;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateItemCommand;

public record RegisterRoutingTemplateItemRequest(
    @NotNull Integer sequence,
    @NotBlank String deliveryStatus,
    @NotNull Boolean required
) {

  public RegisterRoutingTemplateItemCommand toCommand() {
    return new RegisterRoutingTemplateItemCommand(
        this.sequence,
        DeliveryStatus.valueOf(this.deliveryStatus),
        this.required);
  }

}
