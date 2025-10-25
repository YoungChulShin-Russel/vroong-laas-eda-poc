package vroong.laas.order.data.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.core.enums.order.OrderStatus;
import vroong.laas.order.data.entity.ConcurrentEntity;

// OrderStatus는 Infrastructure와 Domain 양쪽에 존재하므로 주의
// - Infrastructure OrderStatus: 이 파일에서 사용 (JPA Entity용)
// - Domain OrderStatus: toDomain()에서 Domain Order 생성 시 사용

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends ConcurrentEntity {

  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private OrderStatus status;

  @Column(name = "ordered_at", nullable = false)
  private Instant orderedAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  @Builder
  public OrderEntity(
      String orderNumber,
      OrderStatus status,
      Instant orderedAt,
      Instant deliveredAt,
      Instant cancelledAt) {
    this.orderNumber = orderNumber;
    this.status = status;
    this.orderedAt = orderedAt;
    this.deliveredAt = deliveredAt;
    this.cancelledAt = cancelledAt;
  }

  public void cancel() {
    this.status = OrderStatus.CANCELLED;
    this.cancelledAt = Instant.now();
  }


}

