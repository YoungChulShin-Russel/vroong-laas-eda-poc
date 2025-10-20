package vroong.laas.order.core.service.infrastructure.external.address;

import org.springframework.stereotype.Component;
import vroong.laas.order.core.service.domain.address.required.AddressRefineClient;
import vroong.laas.order.core.service.domain.shared.Address;
import vroong.laas.order.core.service.domain.shared.LatLng;

@Component
public class DymmyAddressRefineClient implements AddressRefineClient {

  @Override
  public Address refineByReverseGeocoding(LatLng latLng, Address originalAddress) {

    return originalAddress;
  }
}
