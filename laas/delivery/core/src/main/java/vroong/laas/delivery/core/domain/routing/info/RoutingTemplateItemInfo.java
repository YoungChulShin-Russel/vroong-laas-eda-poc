package vroong.laas.delivery.core.domain.routing.info;

import vroong.laas.delivery.core.domain.delivery.DeliveryStatus;
import vroong.laas.delivery.core.domain.routing.RoutingTemplateItem;

public record RoutingTemplateItemInfo(
    Integer sequence,
    DeliveryStatus deliveryStatus,
    Boolean required
) {

  public static RoutingTemplateItemInfo fromEntity(RoutingTemplateItem entity) {
    return new RoutingTemplateItemInfo(
        entity.getSequence(),
        entity.getDeliveryStatus(),
        entity.getRequired());
  }
}
