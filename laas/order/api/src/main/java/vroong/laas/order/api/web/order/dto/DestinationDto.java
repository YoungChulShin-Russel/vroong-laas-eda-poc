package vroong.laas.order.api.web.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import vroong.laas.order.api.web.shared.dto.AddressDto;
import vroong.laas.order.api.web.shared.dto.ContactDto;
import vroong.laas.order.api.web.shared.dto.LatLngDto;
import vroong.laas.order.core.service.domain.order.Destination;

/**
 * 도착지 DTO
 */
public record DestinationDto(
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

  /** Destination Domain → DestinationDto 변환 */
  public static DestinationDto from(Destination destination) {
    return new DestinationDto(
        new ContactDto(
            destination.contact().name(), destination.contact().phoneNumber()),
        new AddressDto(
            destination.address().jibnunAddress(),
            destination.address().roadAddress(),
            destination.address().detailAddress()),
        new LatLngDto(
            destination.latLng().latitude(), destination.latLng().longitude()));
  }

  /** DestinationDto → Destination Domain 변환 */
  public Destination toDomain() {
    return new Destination(
        contact.toDomain(),
        address.toDomain(),
        latLng.toDomain());
  }
}
