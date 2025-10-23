package vroong.laas.bff.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Dispatch Service Client
 * 배차 서비스 API 호출
 */
@Slf4j
@Component
public class DispatchServiceClient extends MsaClient {

    public DispatchServiceClient(
            WebClient webClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Value("${bff.services.dispatch-service.base-url}") String baseUrl,
            @Value("${bff.command.msa-timeout-ms:10000}") long timeoutMs) {
        super(webClient, circuitBreakerRegistry, "dispatch-service", baseUrl, Duration.ofMillis(timeoutMs));
    }

    /**
     * 기사 배차 제안 수락
     * POST /api/v1/dispatches/response
     */
    public Mono<Map<String, Object>> acceptDispatch(Map<String, Object> responseRequest) {
        log.info("[DispatchService] Accepting dispatch");
        return post("/api/v1/dispatches/response", responseRequest, Map.class);
    }
}

