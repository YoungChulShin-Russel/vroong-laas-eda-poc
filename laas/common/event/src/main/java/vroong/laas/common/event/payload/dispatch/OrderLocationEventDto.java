package vroong.laas.common.event.payload.dispatch;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class OrderLocationEventDto {
  private final String contactName;
  private final String contactPhoneNumber;
  private final BigDecimal latitude;
  private final BigDecimal longitude;
  private final String jibunAddress;
  private final String roadAddress;
  private final String detailAddress;
}
