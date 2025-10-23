package vroong.laas.readmodel.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.dto.DispatchServiceResponse;

import java.time.Duration;

/**
 * Dispatch Service Client (Reactive)
 * 
 * 역할: Write Service(Dispatch Service)의 Query API 호출
 * 용도: Fallback (Read Model에 데이터가 없을 때)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchServiceClient {

    private final WebClient webClient;
    
    @Value("${msa.dispatch.url:http://localhost:8082}")
    private String dispatchServiceUrl;
    
    /**
     * Dispatch Service에서 Order의 Dispatch 조회 (Fallback용, Reactive)
     * 
     * @param orderId Order ID
     * @return Mono<DispatchServiceResponse>
     */
    public Mono<DispatchServiceResponse> getDispatchByOrderId(Long orderId) {
        String url = dispatchServiceUrl + "/api/v1/dispatches/order/" + orderId;
        
        log.info("Fallback to Dispatch Service: orderId={}, url={}", orderId, url);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(DispatchServiceResponse.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> log.info("Fallback success (Dispatch): orderId={}", orderId))
                .doOnError(e -> log.error("Fallback failed (Dispatch): orderId={}, error={}", orderId, e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Dispatch fallback error, returning empty: orderId={}", orderId);
                    return Mono.empty();
                });
    }
}

