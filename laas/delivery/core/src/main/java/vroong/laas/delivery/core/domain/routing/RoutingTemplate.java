package vroong.laas.delivery.core.domain.routing;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.delivery.core.domain.ConcurrentEntity;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateCommand;
import vroong.laas.delivery.core.domain.routing.command.RegisterRoutingTemplateItemCommand;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "routing_templates")
public class RoutingTemplate extends ConcurrentEntity {

  @Column(name = "code")
  private String code;

  @Column(name = "description")
  private String description;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "routingTemplate",
      cascade = CascadeType.PERSIST)
  private List<RoutingTemplateItem> items = new ArrayList<>();

  private RoutingTemplate(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public static RoutingTemplate register(RegisterRoutingTemplateCommand command) {
    RoutingTemplate routingTemplate = new RoutingTemplate(command.code(), command.description());

    for (RegisterRoutingTemplateItemCommand item : command.items()) {
      routingTemplate.items.add(
          new RoutingTemplateItem(
              routingTemplate,
              item.sequence(),
              item.deliveryStatus(),
              item.required()));
    }

    return routingTemplate;
  }
}
