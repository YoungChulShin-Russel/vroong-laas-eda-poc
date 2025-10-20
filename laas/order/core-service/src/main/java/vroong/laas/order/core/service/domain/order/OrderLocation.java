package vroong.laas.order.core.service.domain.order;

import lombok.Getter;
import vroong.laas.order.core.service.domain.shared.Address;
import vroong.laas.order.core.service.domain.shared.Contact;
import vroong.laas.order.core.service.domain.shared.LatLng;
import vroong.laas.order.data.entity.order.OrderLocationEntity;

@Getter
public class OrderLocation {

  private final Long id;

  private final Long orderId;

  private Origin origin;

  private Destination destination;

  public OrderLocation(Long id, Long orderId, Origin origin, Destination destination) {
    this.id = id;
    this.orderId = orderId;
    this.origin = origin;
    this.destination = destination;
  }

  public static OrderLocation fromEntity(OrderLocationEntity entity) {
    return new OrderLocation(
        entity.getId(),
        entity.getOrderId(),
        new Origin(
            new Contact(
                entity.getOriginContactName(),
                entity.getOriginContactPhoneNumber()),
            new Address(
                entity.getOriginJibnunAddress(),
                entity.getOriginRoadAddress(),
                entity.getOriginDetailAddress()),
            new LatLng(
                entity.getOriginLatitude(),
                entity.getOriginLongitude())),
        new Destination(
            new Contact(
                entity.getDestinationContactName(),
                entity.getDestinationContactPhoneNumber()),
            new Address(
                entity.getDestinationJibnunAddress(),
                entity.getDestinationRoadAddress(),
                entity.getDestinationDetailAddress()),
            new LatLng(
                entity.getDestinationLatitude(),
                entity.getDestinationLongitude())));
  }
}
