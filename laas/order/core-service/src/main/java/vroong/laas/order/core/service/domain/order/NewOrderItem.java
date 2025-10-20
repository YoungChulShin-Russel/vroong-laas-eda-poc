package vroong.laas.order.core.service.domain.order;

import vroong.laas.order.core.service.domain.shared.Money;

public record NewOrderItem(
    String itemName,
    int quantity,
    Money price
) {

}
