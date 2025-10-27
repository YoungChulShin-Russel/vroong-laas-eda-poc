package vroong.laas.readmodel.common.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
public class OrderTimelineEvent {
    
    private String id;
    private Long orderId;
    private Instant timestamp;
    private String eventType;
    private String description;
    private ChangeInfo changeInfo;
    private Instant createdAt;
    
    @Getter
    @Builder
    public static class ChangeInfo {
        private String field;
        private Object before;
        private Object after;
        private String reason;
    }
}