package vroong.laas.readmodel.query.controller.response;

import java.math.BigDecimal;

public record OrderLocationResponse(
    String contactName,
    String contactPhoneNumber,
    BigDecimal latitude,
    BigDecimal longitude,
    String jibunAddress,
    String roadAddress,
    String detailAddress
) {

}
