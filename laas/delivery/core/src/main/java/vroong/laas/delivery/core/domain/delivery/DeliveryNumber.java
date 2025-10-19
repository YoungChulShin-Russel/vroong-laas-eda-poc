package vroong.laas.delivery.core.domain.delivery;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.format.DateTimeFormatter;

/**
 * 배송번호 Value Object
 *
 * <p>배송번호 형식: DEL-YYMMDDHHMMSS + 랜덤 3자리
 * 예시: DEL-250112143000123
 */
public record DeliveryNumber(String value) {

    private static final String PREFIX = "DEL-";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public DeliveryNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("배송번호는 필수입니다");
        }
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("배송번호는 " + PREFIX + "로 시작해야 합니다");
        }
    }

    /**
     * 기존 배송번호로 생성
     */
    public static DeliveryNumber of(String value) {
        return new DeliveryNumber(value);
    }

    @Converter
    public static class DeliveryNumberConverter implements
        AttributeConverter<DeliveryNumber, String> {

        @Override
        public String convertToDatabaseColumn(DeliveryNumber attribute) {
            return attribute != null ? attribute.value() : null;
        }

        @Override
        public DeliveryNumber convertToEntityAttribute(String dbData) {
            return dbData != null ? DeliveryNumber.of(dbData) : null;
        }
    }
}
