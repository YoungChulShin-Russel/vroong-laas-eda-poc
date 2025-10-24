package vroong.laas.delivery.infrastructure.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.cfg.DateTimeFeature;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtil {

  private static final tools.jackson.databind.json.JsonMapper JSON_MAPPER = tools.jackson.databind.json.JsonMapper.builder()
      .propertyNamingStrategy(tools.jackson.databind.PropertyNamingStrategies.SNAKE_CASE)
      .changeDefaultPropertyInclusion(
          incl -> {
            incl.withContentInclusion(JsonInclude.Include.NON_NULL);
            incl.withValueInclusion(JsonInclude.Include.NON_NULL);
            return incl;
          })
      .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  public static tools.jackson.databind.json.JsonMapper jsonMapper() {
    return JSON_MAPPER;
  }

  public static String toJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return JSON_MAPPER.writeValueAsString(value);
    } catch (JacksonException ex) {
      throw new IllegalArgumentException("JSON 직렬화에 실패했습니다.", ex);
    }
  }

  public static <T> T fromJson(String json, Class<T> type) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return JSON_MAPPER.readValue(json, type);
    } catch (JacksonException ex) {
      throw new IllegalArgumentException("JSON 역직렬화에 실패했습니다.", ex);
    }
  }

  public static <T> T fromJson(String json, tools.jackson.core.type.TypeReference<T> type) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return JSON_MAPPER.readValue(json, type);
    } catch (JacksonException ex) {
      throw new IllegalArgumentException("JSON 역직렬화에 실패했습니다.", ex);
    }
  }
}
