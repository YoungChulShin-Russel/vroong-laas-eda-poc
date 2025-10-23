package vroong.laas.readmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Dispatch Service API 응답 포맷
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchServiceResponse {
    
    private boolean success;
    private DispatchServiceData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispatchServiceData {
        private Long dispatchId;
        private Long orderId;
        private Long agentId;
        private BigDecimal deliveryFee;
        private Instant dispatchedAt;
        private String status;
    }
}

