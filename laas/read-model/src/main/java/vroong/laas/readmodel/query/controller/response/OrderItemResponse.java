package vroong.laas.readmodel.query.controller.response;

import java.math.BigDecimal;

public record OrderItemResponse(
    String itemName,
    Integer quantity,
    BigDecimal price
) {

}
