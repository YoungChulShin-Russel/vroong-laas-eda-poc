package vroong.laas.bff.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
public enum ErrorCode {
    
    // 공통 에러
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    
    // Query 관련 에러
    QUERY_NOT_FOUND("조회 결과를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUERY_TIMEOUT("조회 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT),
    REDIS_CONNECTION_ERROR("Redis 연결 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    MONGO_CONNECTION_ERROR("MongoDB 연결 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    
    // Command 관련 에러
    COMMAND_EXECUTION_FAILED("명령 실행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MSA_SERVICE_UNAVAILABLE("MSA 서비스에 연결할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    MSA_TIMEOUT("MSA 서비스 응답 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT);
    
    private final String message;
    private final HttpStatus httpStatus;
    
    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

