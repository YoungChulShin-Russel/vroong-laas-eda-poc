package vroong.laas.common.event.payload.dispatch;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class OrderItemEventDto {
  private final String itemName;
  private final Integer quantity;
  private final BigDecimal price;
}
