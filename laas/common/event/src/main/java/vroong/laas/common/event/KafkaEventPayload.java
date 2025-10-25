package vroong.laas.common.event;

import java.time.Instant;

public interface KafkaEventPayload {

  SchemaVersion getSchemaVersion();
}
