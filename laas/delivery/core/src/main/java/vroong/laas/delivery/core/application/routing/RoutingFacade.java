package vroong.laas.delivery.core.application.routing;

import lombok.RequiredArgsConstructor;
import vroong.laas.delivery.core.common.annotation.Facade;
import vroong.laas.delivery.core.domain.routing.RoutingTemplateModifyService;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateCommand;
import vroong.laas.delivery.core.domain.routing.info.RoutingTemplateInfo;

@Facade
@RequiredArgsConstructor
public class RoutingFacade {

  private final RoutingTemplateModifyService modifyService;
  private final RoutingTemplateModifyService routingTemplateModifyService;

  public RoutingTemplateInfo createTemplate(RegisterRoutingTemplateCommand command) {
    return routingTemplateModifyService.registerRoutingTemplate(command);
  }
}
