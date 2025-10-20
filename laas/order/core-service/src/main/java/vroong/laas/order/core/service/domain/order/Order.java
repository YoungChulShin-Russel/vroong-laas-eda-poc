package vroong.laas.order.core.service.domain.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import vroong.laas.order.core.enums.order.OrderStatus;
import vroong.laas.order.data.entity.order.OrderEntity;
import vroong.laas.order.data.entity.order.OrderItemEntity;
import vroong.laas.order.data.entity.order.OrderLocationEntity;

@Getter
@ToString
public class Order {

  private final Long id;
  private final OrderNumber orderNumber;
  private OrderStatus status;
  private final List<OrderItem> items;
  private OrderLocation location;
  private final Instant orderedAt;
  private Instant deliveredAt;
  private Instant cancelledAt;

  /**
   * 생성자 (순수 객체 생성)
   *
   * <p>Infrastructure에서 DB 데이터 복원 시 사용
   * <p>테스트에서 다양한 상태의 Order 생성 시 사용
   * <p>도메인 이벤트를 추가하지 않음
   */
  public Order(
      Long id,
      OrderNumber orderNumber,
      OrderStatus status,
      List<OrderItem> items,
      OrderLocation location,
      Instant orderedAt,
      Instant deliveredAt,
      Instant cancelledAt) {
    // 필수 값 체크
    if (id == null) {
      throw new IllegalArgumentException("ID는 필수입니다");
    }
    if (orderNumber == null) {
      throw new IllegalArgumentException("주문번호는 필수입니다");
    }
    if (location == null) {
      throw new IllegalArgumentException("주소 정보는 필수입니다");
    }

    this.id = id;
    this.orderNumber = orderNumber;
    this.status = status;
    this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    this.location = location;
    this.orderedAt = orderedAt;
    this.deliveredAt = deliveredAt;
    this.cancelledAt = cancelledAt;
  }

  public static Order fromEntity(
      OrderEntity orderEntity,
      List<OrderItemEntity> orderItemEntities,
      OrderLocationEntity orderLocationEntity
  ) {
    return new Order(
        orderEntity.getId(),
        OrderNumber.of(orderEntity.getOrderNumber()),
        orderEntity.getStatus(),
        orderItemEntities.stream().map(OrderItem::fromEntity).toList(),
        OrderLocation.fromEntity(orderLocationEntity),
        orderEntity.getOrderedAt(),
        orderEntity.getDeliveredAt(),
        orderEntity.getCancelledAt());
  }
}
