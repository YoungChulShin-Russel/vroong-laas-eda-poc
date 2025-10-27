package vroong.laas.readmodel.query.controller.response;

import lombok.Builder;
import lombok.Getter;
import vroong.laas.readmodel.common.model.OrderTimelineEvent;

import java.time.Instant;

@Getter
@Builder
public class OrderTimelineResponse {

    private final Instant timestamp;
    private final String eventType;
    private final String description;
    private final ChangeInfoResponse changeInfo;
    
    @Getter
    @Builder
    public static class ChangeInfoResponse {
        private final String field;
        private final Object before;
        private final Object after;
        private final String reason;
        
        public static ChangeInfoResponse fromModel(OrderTimelineEvent.ChangeInfo changeInfo) {
            if (changeInfo == null) return null;
            
            return ChangeInfoResponse.builder()
                    .field(changeInfo.getField())
                    .before(changeInfo.getBefore())
                    .after(changeInfo.getAfter())
                    .reason(changeInfo.getReason())
                    .build();
        }
    }
    
    public static OrderTimelineResponse fromModel(OrderTimelineEvent event) {
        return OrderTimelineResponse.builder()
                .timestamp(event.getTimestamp())
                .eventType(event.getEventType())
                .description(event.getDescription())
                .changeInfo(ChangeInfoResponse.fromModel(event.getChangeInfo()))
                .build();
    }
}