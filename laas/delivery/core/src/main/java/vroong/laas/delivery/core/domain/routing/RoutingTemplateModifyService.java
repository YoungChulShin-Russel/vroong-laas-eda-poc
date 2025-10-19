package vroong.laas.delivery.core.domain.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateCommand;
import vroong.laas.delivery.core.domain.routing.info.RoutingTemplateInfo;

@Service
@RequiredArgsConstructor
public class RoutingTemplateModifyService {

  private final RoutingTemplateRepository repository;

  @Transactional
  public RoutingTemplateInfo registerRoutingTemplate(RegisterRoutingTemplateCommand command) {
    RoutingTemplate routingTemplate = RoutingTemplate.register(command);
    repository.save(routingTemplate);

    return RoutingTemplateInfo.fromEntity(routingTemplate);
  }

}
