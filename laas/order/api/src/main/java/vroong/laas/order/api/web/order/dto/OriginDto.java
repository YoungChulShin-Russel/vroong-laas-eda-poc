package vroong.laas.order.api.web.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import vroong.laas.order.api.web.shared.dto.AddressDto;
import vroong.laas.order.api.web.shared.dto.ContactDto;
import vroong.laas.order.api.web.shared.dto.LatLngDto;
import vroong.laas.order.core.service.domain.order.Origin;

/**
 * 출발지 DTO
 */
public record OriginDto(
    @NotNull(message = "연락처는 필수입니다")
    @Valid
    ContactDto contact,

    @NotNull(message = "주소는 필수입니다")
    @Valid
    AddressDto address,

    @NotNull(message = "위경도는 필수입니다")
    @Valid
    LatLngDto latLng
) {

  /** OriginDto → Origin Domain 변환 */
  public Origin toDomain() {
    return new Origin(
        contact.toDomain(),
        address.toDomain(),
        latLng.toDomain());
  }
}
