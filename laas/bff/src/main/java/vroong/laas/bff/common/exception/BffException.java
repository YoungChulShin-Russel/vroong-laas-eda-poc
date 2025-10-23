package vroong.laas.bff.common.exception;

/**
 * BFF 서버 공통 예외 클래스
 */
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
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

