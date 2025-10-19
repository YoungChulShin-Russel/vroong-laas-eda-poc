package vroong.laas.delivery.core.domain.delivery.command;

import java.math.BigDecimal;

public record RegisterDeliveryCommand(
    Long dispatchId,
    Long orderId,
    Long agentId,
    BigDecimal deliveryFee
) {

}
