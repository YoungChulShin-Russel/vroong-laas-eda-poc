package vroong.laas.order.core.service.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.service.domain.address.AddressRefiner;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.Origin;
import vroong.laas.order.core.service.domain.shared.Address;

@Component
@RequiredArgsConstructor
public class OrderAddressRefiner {

  private final AddressRefiner addressRefiner;

  /**
   * Origin 주소를 정제합니다.
   *
   * <p>역지오코딩을 통해 정제된 주소로 Origin을 재생성합니다.
   *
   * @param origin 원본 Origin
   * @return 정제된 주소가 적용된 Origin
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 역지오코딩 실패 시
   */
  public Origin refineOrigin(Origin origin) {
    Address refinedAddress = addressRefiner.refine(origin.latLng(), origin.address());
    return new Origin(origin.contact(), refinedAddress, origin.latLng());
  }

  /**
   * Destination 주소를 정제합니다.
   *
   * <p>역지오코딩을 통해 정제된 주소로 Destination을 재생성합니다.
   *
   * @param destination 원본 Destination
   * @return 정제된 주소가 적용된 Destination
   * @throws vroong.laas.order.core.domain.address.exception.AddressRefineFailedException 역지오코딩 실패 시
   */
  public Destination refineDestination(Destination destination) {
    Address refinedAddress = addressRefiner.refine(destination.latLng(), destination.address());
    return new Destination(
        destination.contact(), refinedAddress, destination.latLng());
  }
}
