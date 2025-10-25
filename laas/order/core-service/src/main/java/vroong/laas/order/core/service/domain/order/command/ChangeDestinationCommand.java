package vroong.laas.order.core.service.domain.order.command;

import vroong.laas.order.core.service.domain.order.Destination;

public record ChangeDestinationCommand(
    Long orderId,
    Destination newDestination
) {

}
