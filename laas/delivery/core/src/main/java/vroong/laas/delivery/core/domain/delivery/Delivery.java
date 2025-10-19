package vroong.laas.delivery.core.domain.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.delivery.core.domain.ConcurrentEntity;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;

@Getter
@Entity
@Table(name = "deliveries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends ConcurrentEntity {

  @Column(name = "delivery_number")
  @Convert(converter = DeliveryNumber.DeliveryNumberConverter.class)
  private DeliveryNumber deliveryNumber;

  @Column(name = "order_id")
  private Long orderId;

  @Column(name = "agent_id")
  private Long agentId;

  @Column(name = "delivery_fee")
  private BigDecimal deliveryFee;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private DeliveryStatus status;

  private Delivery(
      DeliveryNumber deliveryNumber,
      Long orderId,
      Long agentId,
      BigDecimal deliveryFee,
      DeliveryStatus status) {
    this.deliveryNumber = deliveryNumber;
    this.orderId = orderId;
    this.agentId = agentId;
    this.deliveryFee = deliveryFee;
    this.status = status;
  }

  public static Delivery register(
      RegisterDeliveryCommand command,
      DeliveryNumberGenerator deliveryNumberGenerator) {
    DeliveryNumber deliveryNumber = deliveryNumberGenerator.generate();

    return new Delivery(
        deliveryNumber,
        command.orderId(),
        command.agentId(),
        command.deliveryFee(),
        DeliveryStatus.STARTED);
  }
}
