package vroong.laas.common.event;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaEvent<T extends KafkaEventPayload> {

  /**
   * 이벤트 아이디 (UUID)
   */
  private final String eventId;

  /**
   * 이벤트 타입
   */
  private final KafkaEventType type;

  /**
   * 이벤트 소스 (이벤트 발행 서버)
   */
  private final KafkaEventSource source;

  /**
   * 발행 시간
   */
  private final long timestamp;

  /**
   * 스키마 버전
   */
  private final SchemaVersion schemaVersion;

  /**
   * 이벤트 데이터
   */
  private final T payload;


  public static KafkaEvent<KafkaEventPayload> of(
      KafkaEventType kafkaEventType,
      KafkaEventSource kafkaEventSource,
      KafkaEventPayload kafkaEventPayload) {
    return KafkaEvent.of(
        UUID.randomUUID().toString(),
        kafkaEventType,
        kafkaEventSource,
        kafkaEventPayload);
  }

  public static KafkaEvent<KafkaEventPayload> of(
      String eventId,
      KafkaEventType kafkaEventType,
      KafkaEventSource kafkaEventSource,
      KafkaEventPayload kafkaEventPayload) {
    return new KafkaEvent<>(
        eventId,
        kafkaEventType,
        kafkaEventSource,
        System.currentTimeMillis(),
        kafkaEventPayload.getSchemaVersion(),
        kafkaEventPayload);
  }

  /**
   * Event 객체를 Json 문자열로 직렬화 합니다.
   */
  public String toJson() {
    return DataSerializer.serialize(this);
  }

  public static KafkaEvent<KafkaEventPayload> fromJson(String json) {
    EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
    if (eventRaw == null) {
      return null;
    }

    KafkaEventType kafkaEventType = KafkaEventType.from(eventRaw.getType());
    KafkaEventSource kafkaEventSource = KafkaEventSource.from(eventRaw.getSource());
    SchemaVersion schemaVersion = SchemaVersion.from(eventRaw.getSchemaVersion());
    KafkaEventPayload kafkaEventPayload =
        DataSerializer.deserialize(eventRaw.getPayload(), kafkaEventType.getPayloadClass());

    return new KafkaEvent<>(
        eventRaw.getEventId(),
        kafkaEventType,
        kafkaEventSource,
        eventRaw.getTimestamp(),
        schemaVersion,
        kafkaEventPayload);
  }

  @Getter
  private static class EventRaw {

    private String eventId;
    private String type;
    private String source;
    private long timestamp;
    private String schemaVersion;
    private Object payload;
  }
}
