package vroong.laas.order.api.web.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.order.api.web.common.response.ApiResponse;
import vroong.laas.order.api.web.order.request.ChangeDestinationRequest;
import vroong.laas.order.api.web.order.request.CreateOrderRequest;
import vroong.laas.order.api.web.order.response.OrderIdResponse;
import vroong.laas.order.api.web.order.response.OrderResponse;
import vroong.laas.order.core.service.application.order.OrderFacade;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.core.service.domain.order.query.GetOrderQuery;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderFacade orderFacade;

  @PostMapping
  public ApiResponse<OrderIdResponse> createOrder(
      @Valid @RequestBody CreateOrderRequest request) {
    Long orderId = orderFacade.createOrder(request.toCommand());

    return ApiResponse.success(new OrderIdResponse(orderId));
  }

  @PutMapping("/{orderId}/destination")
  public ApiResponse<Void> changeDestination(
      @PathVariable Long orderId,
      @Valid @RequestBody ChangeDestinationRequest request) {
    orderFacade.changeDestination(request.toCommand(orderId));

    return ApiResponse.success();
  }

  @GetMapping("/{orderId}")
  public ApiResponse<OrderResponse> getOrder(@PathVariable Long orderId) {
    Order order = orderFacade.getOrder(new GetOrderQuery(orderId));

    return ApiResponse.success(OrderResponse.from(order));
  }


}
