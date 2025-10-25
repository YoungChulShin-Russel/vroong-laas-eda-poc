package vroong.laas.delivery.api.web.delivery;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.delivery.api.web.common.response.ApiResponse;
import vroong.laas.delivery.api.web.delivery.request.CancelDeliveryRequest;
import vroong.laas.delivery.api.web.delivery.request.RegisterDeliveryRequest;
import vroong.laas.delivery.api.web.delivery.response.DeliveryIdResponse;
import vroong.laas.delivery.core.application.delivery.DeliveryFacade;
import vroong.laas.delivery.core.domain.delivery.command.DeliverDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.PickupDeliveryCommand;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deliveries/")
public class DeliveryController {

  private final DeliveryFacade deliveryFacade;

  @PostMapping("/register")
  public ApiResponse<DeliveryIdResponse> registerDelivery(
      @Valid @RequestBody RegisterDeliveryRequest request
  ) {
    Long deliveryId = deliveryFacade.registerDelivery(request.toCommand());

    return ApiResponse.success(new  DeliveryIdResponse(deliveryId));
  }

  @PostMapping("/{deliveryId}/pickup")
  public ApiResponse<Void> pickupDelivery(
      @PathVariable Long deliveryId
  ) {
    deliveryFacade.pickupDelivery(new PickupDeliveryCommand(deliveryId));

    return ApiResponse.success(null);
  }

  @PostMapping("/{deliveryId}/deliver")
  public ApiResponse<Void> deliverDelivery(
      @PathVariable Long deliveryId
  ) {
    deliveryFacade.deliverDelivery(new DeliverDeliveryCommand(deliveryId));

    return ApiResponse.success(null);
  }

  @PostMapping("/{deliveryId}/cancel")
  public ApiResponse<Void> cancelDelivery(
      @PathVariable Long deliveryId,
      @Valid @RequestBody CancelDeliveryRequest request
  ) {
    deliveryFacade.cancelDelivery(request.toCommand(deliveryId));

    return ApiResponse.success(null);
  }
}
