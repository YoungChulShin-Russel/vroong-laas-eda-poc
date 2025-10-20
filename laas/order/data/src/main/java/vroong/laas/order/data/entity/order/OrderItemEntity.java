package vroong.laas.order.data.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.data.entity.BaseEntity;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseEntity {

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "item_name", nullable = false, length = 200)
  private String itemName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Builder
  public OrderItemEntity(
      Long orderId,
      String itemName,
      Integer quantity,
      BigDecimal price) {
    this.orderId = orderId;
    this.itemName = itemName;
    this.quantity = quantity;
    this.price = price;
  }
}

