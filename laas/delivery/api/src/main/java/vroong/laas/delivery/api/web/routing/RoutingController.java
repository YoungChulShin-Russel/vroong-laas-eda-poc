package vroong.laas.delivery.api.web.routing;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.delivery.api.web.routing.request.RegisterRoutingTemplateRequest;
import vroong.laas.delivery.api.web.routing.response.RegisterRoutingTemplateResponse;
import vroong.laas.delivery.core.application.routing.RoutingFacade;
import vroong.laas.delivery.core.domain.routing.info.RoutingTemplateInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routings/")
public class RoutingController {

  private final RoutingFacade routingFacade;

  @PostMapping("/templates")
  public RegisterRoutingTemplateResponse registerRoutingTemplate(
      @Valid @RequestBody RegisterRoutingTemplateRequest request
  ) {
    RoutingTemplateInfo routingTemplateInfo = routingFacade.registerTemplate(request.toCommand());

    return new RegisterRoutingTemplateResponse(routingTemplateInfo.routingTemplateId());
  }
}
