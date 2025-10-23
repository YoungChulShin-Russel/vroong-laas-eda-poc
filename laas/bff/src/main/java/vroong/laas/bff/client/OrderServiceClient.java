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
 * Order Service Client
 * 주문 서비스 API 호출
 */
@Slf4j
@Component
public class OrderServiceClient extends MsaClient {

    public OrderServiceClient(
            WebClient webClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Value("${bff.services.order-service.base-url}") String baseUrl,
            @Value("${bff.command.msa-timeout-ms:10000}") long timeoutMs) {
        super(webClient, circuitBreakerRegistry, "order-service", baseUrl, Duration.ofMillis(timeoutMs));
    }

    /**
     * 주문 생성
     * POST /api/v1/orders
     */
    public Mono<Map<String, Object>> createOrder(Map<String, Object> orderRequest) {
        log.info("[OrderService] Creating order");
        return post("/api/v1/orders", orderRequest, Map.class);
    }
}

