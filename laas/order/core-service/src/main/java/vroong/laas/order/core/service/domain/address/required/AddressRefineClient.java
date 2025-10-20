package vroong.laas.order.core.service.domain.address.required;

import vroong.laas.order.core.service.domain.shared.Address;
import vroong.laas.order.core.service.domain.shared.LatLng;

public interface AddressRefineClient {

  Address refineByReverseGeocoding(LatLng latLng, Address originalAddress);

}
