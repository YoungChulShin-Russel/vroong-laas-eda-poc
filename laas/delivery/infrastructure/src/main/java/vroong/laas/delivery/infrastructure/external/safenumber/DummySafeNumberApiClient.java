package vroong.laas.delivery.infrastructure.external.safenumber;

import org.springframework.stereotype.Component;
import vroong.laas.delivery.core.domain.safenumber.SafeNumber;
import vroong.laas.delivery.core.domain.safenumber.SafeNumberApiClient;

@Component
class DummySafeNumberApiClient implements SafeNumberApiClient {

  @Override
  public SafeNumber publish(String originalNumber) {
    return new SafeNumber("050123456789");
  }
}
