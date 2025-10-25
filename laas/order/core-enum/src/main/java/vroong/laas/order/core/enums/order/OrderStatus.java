package vroong.laas.order.core.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
  CREATED("생성"),
  CANCELLED("취소");

  private final String description;
}

