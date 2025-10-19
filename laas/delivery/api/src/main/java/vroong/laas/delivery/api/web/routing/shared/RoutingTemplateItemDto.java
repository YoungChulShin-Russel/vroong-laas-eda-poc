package vroong.laas.delivery.api.web.routing.shared;

import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateItemCommand;

public record RoutingTemplateItemDto(
    Integer sequence,
    String deliveryStatus,
    String deliveryStatusName,
    Boolean required
) {
}
