package vroong.laas.order.api.web.common.response;

import lombok.Builder;
import lombok.Getter;
import vroong.laas.order.core.service.common.exception.ErrorCode;

@Getter
@Builder
public class ApiResponse<T> {

  private Result result;
  private T data;
  private String message;
  private String errorCode;

  public enum Result {
    SUCCESS, FAIL
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .result(Result.SUCCESS)
        .data(data)
        .message(message)
        .build();
  }

  public static <T> ApiResponse<T> success() {
    return success(null, null);
  }

  public static <T> ApiResponse<T> success(T data) {
    return success(data, null);
  }

  public static <T> ApiResponse<T> fail(String message, String errorCode) {
    return ApiResponse.<T>builder()
        .result(Result.FAIL)
        .message(message)
        .errorCode(errorCode)
        .build();
  }

  public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
    return ApiResponse.<T>builder()
        .result(Result.FAIL)
        .message(errorCode.getMessage())
        .errorCode(errorCode.name())
        .build();
  }

}
