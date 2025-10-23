package vroong.laas.readmodel.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.dto.ApiResponse;
import vroong.laas.readmodel.exception.OrderNotFoundException;

/**
 * 전역 예외 처리 (Reactive)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * OrderNotFoundException 처리 (Reactive)
     */
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ApiResponse<Void>> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: orderId={}", ex.getOrderId());
        
        return Mono.just(ApiResponse.error("ORDER_NOT_FOUND", ex.getMessage()));
    }

    /**
     * 기타 예외 처리 (Reactive)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        
        return Mono.just(ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다"));
    }
}

