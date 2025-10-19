package vroong.laas.delivery.core.domain.routing.info;

import java.util.List;
import vroong.laas.delivery.core.domain.routing.RoutingTemplate;

public record RoutingTemplateInfo(
    Long routingTemplateId,
    String code,
    String description,
    List<RoutingTemplateItemInfo> items
) {

  public static RoutingTemplateInfo fromEntity(RoutingTemplate entity) {
    return new RoutingTemplateInfo(
        entity.getId(),
        entity.getCode(),
        entity.getDescription(),
        entity.getItems().stream()
            .map(RoutingTemplateItemInfo::fromEntity)
            .toList());
  }
}
