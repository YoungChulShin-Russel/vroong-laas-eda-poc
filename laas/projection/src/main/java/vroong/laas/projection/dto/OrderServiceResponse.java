package vroong.laas.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Service API 응답 포맷
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderServiceResponse {
    
    private boolean success;
    private OrderServiceData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderServiceData {
        private Long orderId;
        private String orderNumber;
    }
}

