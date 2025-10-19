package vroong.laas.delivery.core.domain.routing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.delivery.core.domain.BaseEntity;
import vroong.laas.delivery.core.domain.delivery.DeliveryStatus;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "routing_template_items")
public class RoutingTemplateItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "routing_template_id")
  private RoutingTemplate routingTemplate;

  @Column(name = "sequence")
  private Integer sequence;

  @Column(name = "delivery_status")
  @Enumerated(EnumType.STRING)
  private DeliveryStatus deliveryStatus;

  @Column(name = "required")
  private Boolean required;

  public RoutingTemplateItem(
      RoutingTemplate routingTemplate,
      Integer sequence,
      DeliveryStatus deliveryStatus,
      Boolean required) {
    this.routingTemplate = routingTemplate;
    this.sequence = sequence;
    this.deliveryStatus = deliveryStatus;
    this.required = required;
  }
}
