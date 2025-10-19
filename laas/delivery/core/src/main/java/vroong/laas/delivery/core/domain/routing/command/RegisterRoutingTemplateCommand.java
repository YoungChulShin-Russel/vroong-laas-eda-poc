package vroong.laas.delivery.core.domain.routing.command;

import java.util.List;

public record RegisterRoutingTemplateCommand(
    String code,
    String description,
    List<RegisterRoutingTemplateItemCommand> items
) {

}
