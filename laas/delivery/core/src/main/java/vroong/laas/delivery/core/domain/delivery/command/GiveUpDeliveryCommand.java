package vroong.laas.delivery.core.domain.delivery.command;

public record GiveUpDeliveryCommand(
    Long agentId,
    Long deliveryId,
    String reason
) {

}
