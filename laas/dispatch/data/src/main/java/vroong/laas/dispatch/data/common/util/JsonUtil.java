package vroong.laas.dispatch.data.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtil {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .changeDefaultPropertyInclusion(
          incl -> {
            incl.withContentInclusion(JsonInclude.Include.NON_NULL);
            incl.withValueInclusion(JsonInclude.Include.NON_NULL);
            return incl;
          })
      .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  public static JsonMapper jsonMapper() {
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

  public static <T> T fromJson(String json, TypeReference<T> type) {
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
