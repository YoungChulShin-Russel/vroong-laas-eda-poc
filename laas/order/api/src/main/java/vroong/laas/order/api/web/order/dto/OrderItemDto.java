package vroong.laas.order.api.web.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import vroong.laas.order.core.service.domain.order.NewOrderItem;
import vroong.laas.order.core.service.domain.shared.Money;

/**
 * 주문 아이템 DTO
 */
public record OrderItemDto(
    @NotBlank(message = "상품명은 필수입니다")
    String itemName,

    @Positive(message = "수량은 1개 이상이어야 합니다")
    int quantity,

    @NotNull(message = "가격은 필수입니다")
    @Positive(message = "가격은 0보다 커야 합니다")
    BigDecimal price
) {

  /** OrderItemDto → OrderItem Domain 변환 */
  public NewOrderItem toNewOrderItem() {
    return new NewOrderItem(
        itemName,
        quantity,
        new Money(price));
  }
}
