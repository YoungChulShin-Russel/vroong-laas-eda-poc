package vroong.laas.delivery.api.web.routing.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateCommand;

public record RegisterRoutingTemplateRequest(
    @NotBlank String code,
    @NotBlank String description,
    @Valid @NotNull List<RegisterRoutingTemplateItemRequest> items
) {

  public RegisterRoutingTemplateCommand toCommand() {
    return new RegisterRoutingTemplateCommand(
        this.code,
        this.description,
        items.stream()
            .map(RegisterRoutingTemplateItemRequest::toCommand)
            .toList());
  }

}
