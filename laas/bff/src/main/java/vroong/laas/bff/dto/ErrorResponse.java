package vroong.laas.bff.dto;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 */
public record ErrorResponse(
    String error,
    String message,
    LocalDateTime timestamp
) {
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, LocalDateTime.now());
    }
}

