package vroong.laas.dispatch.api.web.common.response;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vroong.laas.dispatch.core.common.exception.BaseException;
import vroong.laas.dispatch.core.common.exception.ErrorCode;

@Slf4j
@RestControllerAdvice
public class CommonControllerAdvice {

  private static final List<ErrorCode> SPECIFIC_ALERT_TARGET_ERROR_CODE_LIST = new ArrayList<>();

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(value = Exception.class)
  public <T> ApiResponse<T> onException(Exception e) {
    return ApiResponse.fail(ErrorCode.COMMON_SYSTEM_ERROR);
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = BaseException.class)
  public <T> ApiResponse<T> onBaseException(BaseException e) {
    if (SPECIFIC_ALERT_TARGET_ERROR_CODE_LIST.contains(e.getErrorCode())) {
      log.error("[BaseException] cause = {}, errorMsg = {}",
          NestedExceptionUtils
              .getMostSpecificCause(e), NestedExceptionUtils.getMostSpecificCause(e).getMessage());
    } else {
      log.warn("[BaseException] cause = {}, errorMsg = {}",
          NestedExceptionUtils.getMostSpecificCause(e),
          NestedExceptionUtils.getMostSpecificCause(e).getMessage());
    }
    return ApiResponse.fail(e.getMessage(), e.getErrorCode().name());
  }

  @ResponseStatus(HttpStatus.OK)
  @ExceptionHandler(value = {ClientAbortException.class})
  public <T> ApiResponse<T> skipException(Exception e) {
    log.warn("[skipException] cause = {}, errorMsg = {}",
        NestedExceptionUtils.getMostSpecificCause(e),
        NestedExceptionUtils.getMostSpecificCause(e).getMessage());
    return ApiResponse.fail(ErrorCode.COMMON_SYSTEM_ERROR);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public <T> ApiResponse<T> methodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.warn("[BaseException] errorMsg = {}",
        NestedExceptionUtils.getMostSpecificCause(e).getMessage());
    BindingResult bindingResult = e.getBindingResult();
    FieldError fe = bindingResult.getFieldError();
    if (fe != null) {
      String message =
          "Request Error" + " " + fe.getField() + "=" + fe.getRejectedValue() + " (" + fe
              .getDefaultMessage() + ")";
      return ApiResponse.fail(message, ErrorCode.INVALID_INPUT.name());
    } else {
      return ApiResponse.fail(
          ErrorCode.INVALID_INPUT.getMessage(),
          ErrorCode.INVALID_INPUT.name());
    }
  }

}
