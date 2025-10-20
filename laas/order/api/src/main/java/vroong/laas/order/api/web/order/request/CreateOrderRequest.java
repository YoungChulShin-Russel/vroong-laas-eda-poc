package vroong.laas.order.api.web.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import vroong.laas.order.api.web.order.dto.DestinationDto;
import vroong.laas.order.api.web.order.dto.OrderItemDto;
import vroong.laas.order.api.web.order.dto.OriginDto;
import vroong.laas.order.core.service.domain.order.NewOrderItem;
import vroong.laas.order.core.service.domain.order.command.CreateOrderCommand;

public record CreateOrderRequest(
    @Valid
    List<OrderItemDto> items,  // 선택 사항

    @NotNull(message = "출발지는 필수입니다")
    @Valid
    OriginDto origin,

    @NotNull(message = "도착지는 필수입니다")
    @Valid
    DestinationDto destination
) {

  public CreateOrderCommand toCommand() {
    List<NewOrderItem> orderItems =
        items != null
            ? items.stream().map(OrderItemDto::toNewOrderItem).toList()
            : List.of();

    return new CreateOrderCommand(
        orderItems,
        origin.toDomain(),
        destination.toDomain());
  }
}
