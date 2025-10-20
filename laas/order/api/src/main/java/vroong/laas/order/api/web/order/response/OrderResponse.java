package vroong.laas.order.api.web.order.response;

import vroong.laas.order.core.service.domain.order.Order;

public record OrderResponse(
    Long orderId,
    String orderNumber
) {

  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getOrderNumber().value());
  }

}
