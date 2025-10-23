package vroong.laas.readmodel.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.dto.DeliveryServiceResponse;

import java.time.Duration;

/**
 * Delivery Service Client (Reactive)
 * 
 * 역할: Write Service(Delivery Service)의 Query API 호출
 * 용도: Fallback (Read Model에 데이터가 없을 때)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryServiceClient {

    private final WebClient webClient;
    
    @Value("${msa.delivery.url:http://localhost:8083}")
    private String deliveryServiceUrl;
    
    /**
     * Delivery Service에서 Order의 Delivery 조회 (Fallback용, Reactive)
     * 
     * @param orderId Order ID
     * @return Mono<DeliveryServiceResponse>
     */
    public Mono<DeliveryServiceResponse> getDeliveryByOrderId(Long orderId) {
        String url = deliveryServiceUrl + "/api/v1/deliveries/order/" + orderId;
        
        log.info("Fallback to Delivery Service: orderId={}, url={}", orderId, url);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(DeliveryServiceResponse.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> log.info("Fallback success (Delivery): orderId={}", orderId))
                .doOnError(e -> log.error("Fallback failed (Delivery): orderId={}, error={}", orderId, e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Delivery fallback error, returning empty: orderId={}", orderId);
                    return Mono.empty();
                });
    }
}

