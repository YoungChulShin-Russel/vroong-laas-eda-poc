package vroong.laas.bff.common.exception;

import lombok.Getter;

/**
 * BFF 서버 공통 예외 클래스
 */
@Getter
public class BffException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BffException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BffException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BffException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    public BffException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

