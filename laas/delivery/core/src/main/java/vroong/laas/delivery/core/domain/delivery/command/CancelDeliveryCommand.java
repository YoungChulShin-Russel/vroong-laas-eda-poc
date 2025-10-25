package vroong.laas.delivery.core.domain.delivery.command;

public record CancelDeliveryCommand(
    Long agentId,
    Long deliveryId,
    String reason
) {

}
