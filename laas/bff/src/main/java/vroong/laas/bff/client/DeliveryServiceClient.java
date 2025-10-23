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
 * Delivery Service Client
 * 배송 서비스 API 호출
 */
@Slf4j
@Component
public class DeliveryServiceClient extends MsaClient {

    public DeliveryServiceClient(
            WebClient webClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Value("${bff.services.delivery-service.base-url}") String baseUrl,
            @Value("${bff.command.msa-timeout-ms:10000}") long timeoutMs) {
        super(webClient, circuitBreakerRegistry, "delivery-service", baseUrl, Duration.ofMillis(timeoutMs));
    }

    /**
     * 기사 상점 도착 (픽업)
     * POST /api/v1/deliveries/{deliveryId}/pickup
     */
    public Mono<Map<String, Object>> pickupAtStore(Long deliveryId) {
        log.info("[DeliveryService] Pickup at store: deliveryId={}", deliveryId);
        return post("/api/v1/deliveries/" + deliveryId + "/pickup", null, Map.class);
    }

    /**
     * 기사 배송 완료
     * POST /api/v1/deliveries/{deliveryId}/deliver
     */
    public Mono<Map<String, Object>> completeDelivery(Long deliveryId) {
        log.info("[DeliveryService] Completing delivery: deliveryId={}", deliveryId);
        return post("/api/v1/deliveries/" + deliveryId + "/deliver", null, Map.class);
    }
}

