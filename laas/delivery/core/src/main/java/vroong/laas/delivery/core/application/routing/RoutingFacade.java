package vroong.laas.delivery.core.application.routing;

import lombok.RequiredArgsConstructor;
import vroong.laas.delivery.core.common.annotation.Facade;
import vroong.laas.delivery.core.domain.routing.RoutingModifyService;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateCommand;
import vroong.laas.delivery.core.domain.routing.info.RoutingTemplateInfo;

@Facade
@RequiredArgsConstructor
public class RoutingFacade {

  private final RoutingModifyService routingModifyService;

  public RoutingTemplateInfo registerTemplate(RegisterRoutingTemplateCommand command) {
    return routingModifyService.registerRoutingTemplate(command);
  }
}
