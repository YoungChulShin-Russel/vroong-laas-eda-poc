package vroong.laas.order.core.service.domain.order;


import vroong.laas.order.core.service.domain.shared.Money;
import vroong.laas.order.data.entity.order.OrderItemEntity;

public record OrderItem(
    Long id,
    Long orderId,
    String itemName,
    int quantity,
    Money price) {

  public OrderItem {
    if (itemName == null || itemName.isBlank()) {
      throw new IllegalArgumentException("상품명은 필수입니다");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
    }
    if (price == null) {
      throw new IllegalArgumentException("가격은 필수입니다");
    }
  }

  public static OrderItem fromEntity(OrderItemEntity entity) {
    return new OrderItem(
        entity.getId(),
        entity.getOrderId(),
        entity.getItemName(),
        entity.getQuantity(),
        new Money(entity.getPrice()));
  }
}

