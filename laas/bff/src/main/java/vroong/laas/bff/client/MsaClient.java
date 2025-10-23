package vroong.laas.bff.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vroong.laas.bff.common.exception.BaseException;
import vroong.laas.bff.common.exception.ErrorCode;

import java.time.Duration;

/**
 * MSA 서비스 호출을 위한 기본 클라이언트
 * WebClient + Circuit Breaker 패턴 적용
 */
@Slf4j
public abstract class MsaClient {

    protected final WebClient webClient;
    protected final CircuitBreaker circuitBreaker;
    protected final String serviceName;
    protected final String baseUrl;
    protected final Duration timeout;

    protected MsaClient(
            WebClient webClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            String serviceName,
            String baseUrl,
            Duration timeout) {
        this.webClient = webClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
        this.serviceName = serviceName;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }

    /**
     * GET 요청
     */
    @SuppressWarnings("unchecked")
    protected <T> Mono<T> get(String path, Class<T> responseType) {
        return (Mono<T>) executeRequest(HttpMethod.GET, path, null, responseType);
    }

    /**
     * POST 요청
     */
    @SuppressWarnings("unchecked")
    protected <T> Mono<T> post(String path, Object body, Class<?> responseType) {
        return (Mono<T>) executeRequest(HttpMethod.POST, path, body, responseType);
    }

    /**
     * PUT 요청
     */
    @SuppressWarnings("unchecked")
    protected <T> Mono<T> put(String path, Object body, Class<?> responseType) {
        return (Mono<T>) executeRequest(HttpMethod.PUT, path, body, responseType);
    }

    /**
     * DELETE 요청
     */
    @SuppressWarnings("unchecked")
    protected <T> Mono<T> delete(String path, Class<T> responseType) {
        return (Mono<T>) executeRequest(HttpMethod.DELETE, path, null, responseType);
    }

    /**
     * HTTP 요청 실행 (Circuit Breaker 적용)
     */
    private Mono<?> executeRequest(
            HttpMethod method,
            String path,
            Object body,
            Class<?> responseType) {
        
        String url = baseUrl + path;
        
        log.debug("[{}] {} {}", serviceName, method, url);
        
        return webClient.method(method)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body != null ? body : "")
                .retrieve()
                .bodyToMono(responseType)
                .timeout(timeout)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnSuccess(response -> 
                    log.debug("[{}] Request success: {} {}", serviceName, method, path))
                .doOnError(error -> 
                    log.error("[{}] Request failed: {} {}, error={}", 
                        serviceName, method, path, error.getMessage()))
                .onErrorMap(this::handleError);
    }

    /**
     * 에러 변환
     */
    private Throwable handleError(Throwable error) {
        log.error("[{}] MSA call failed: {}", serviceName, error.getMessage(), error);
        
        if (error instanceof BaseException) {
            return error;
        }
        
        return new BaseException(
            ErrorCode.MSA_SERVICE_UNAVAILABLE,
            String.format("%s service unavailable: %s", serviceName, error.getMessage()),
            error
        );
    }
}

