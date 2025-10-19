package vroong.laas.delivery.core.domain.routing.command;

import vroong.laas.delivery.core.domain.delivery.DeliveryStatus;

public record RegisterRoutingTemplateItemCommand(
    int sequence,
    DeliveryStatus deliveryStatus,
    boolean required
) {

}
