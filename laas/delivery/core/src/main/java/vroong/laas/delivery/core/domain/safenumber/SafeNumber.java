package vroong.laas.delivery.core.domain.safenumber;

public record SafeNumber(
    String value
) {

  public SafeNumber {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value is null or blank");
    }

    if (!value.startsWith("050")) {
      throw new IllegalArgumentException("value is not a number");
    }
  }
}
