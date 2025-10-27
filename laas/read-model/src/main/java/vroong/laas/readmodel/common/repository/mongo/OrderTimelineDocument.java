package vroong.laas.readmodel.common.repository.mongo;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vroong.laas.readmodel.common.model.OrderTimelineEvent;

import java.time.Instant;

/**
 * Order Timeline MongoDB Document
 */
@Getter
@Builder(toBuilder = true)
@Document(collection = "order_timelines")
public class OrderTimelineDocument {
    
    @Id
    private String id;
    
    private Long orderId;
    private Instant timestamp;
    private String eventType;
    private String description;
    private ChangeInfoDocument changeInfo;
    private Instant createdAt;
    
    @Getter
    @Builder
    public static class ChangeInfoDocument {
        private String field;
        private Object before;
        private Object after;
        private String reason;
        
        public static ChangeInfoDocument fromModel(OrderTimelineEvent.ChangeInfo changeInfo) {
            if (changeInfo == null) return null;
            
            return ChangeInfoDocument.builder()
                    .field(changeInfo.getField())
                    .before(changeInfo.getBefore())
                    .after(changeInfo.getAfter())
                    .reason(changeInfo.getReason())
                    .build();
        }
        
        public OrderTimelineEvent.ChangeInfo toModel() {
            return OrderTimelineEvent.ChangeInfo.builder()
                    .field(field)
                    .before(before)
                    .after(after)
                    .reason(reason)
                    .build();
        }
    }
    
    public static OrderTimelineDocument fromModel(OrderTimelineEvent event) {
        return OrderTimelineDocument.builder()
                .id(event.getId())
                .orderId(event.getOrderId())
                .timestamp(event.getTimestamp())
                .eventType(event.getEventType())
                .description(event.getDescription())
                .changeInfo(ChangeInfoDocument.fromModel(event.getChangeInfo()))
                .createdAt(event.getCreatedAt())
                .build();
    }
    
    public OrderTimelineEvent toModel() {
        return OrderTimelineEvent.builder()
                .id(id)
                .orderId(orderId)
                .timestamp(timestamp)
                .eventType(eventType)
                .description(description)
                .changeInfo(changeInfo != null ? changeInfo.toModel() : null)
                .createdAt(createdAt)
                .build();
    }
}