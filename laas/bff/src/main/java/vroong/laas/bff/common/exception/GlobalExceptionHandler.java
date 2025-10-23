package vroong.laas.bff.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BffException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBffException(BffException e) {
        logger.error("BFF Exception: {}", e.getMessage(), e);
        
        return Mono.just(ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(Map.of(
                "error", e.getErrorCode().name(),
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ))
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGeneralException(Exception e) {
        logger.error("Unexpected exception: {}", e.getMessage(), e);
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", "서버 내부 오류가 발생했습니다.",
                "timestamp", LocalDateTime.now()
            ))
        );
    }
}

