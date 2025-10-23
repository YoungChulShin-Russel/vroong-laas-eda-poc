package vroong.laas.readmodel.query.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Delivery Service API 응답 포맷
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryServiceResponse {
    
    private boolean success;
    private DeliveryServiceData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryServiceData {
        private Long deliveryId;
        private Long orderId;
        private String deliveryStatus;
        private Instant deliveryStartedAt;
        private Instant deliveryPickedUpAt;
        private Instant deliveryDeliveredAt;
        private Instant deliveryCancelledAt;
    }
}

