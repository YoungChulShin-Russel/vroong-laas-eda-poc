package vroong.laas.order.api.web.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import vroong.laas.order.api.web.order.dto.DestinationDto;
import vroong.laas.order.core.service.domain.order.command.ChangeDestinationCommand;

public record ChangeDestinationRequest(
    @NotNull(message = "도착지는 필수입니다")
    @Valid
    DestinationDto destination
) {

  public ChangeDestinationCommand toCommand(Long orderId) {
    return new ChangeDestinationCommand(orderId, destination.toDomain());
  }
}
