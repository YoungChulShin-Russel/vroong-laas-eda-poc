package vroong.laas.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public class SchemaVersion implements Comparable<SchemaVersion> {

  private final int major;
  private final int minor;

  public SchemaVersion(int major, int minor) {
    this.major = major;
    this.minor = minor;
  }

  public int major() {
    return major;
  }

  public int minor() {
    return minor;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static SchemaVersion from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("schemaVersion is null");
    }
    String[] parts = value.split("\\.");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Use <major>.<minor>");
    }

    int major = Integer.parseInt(parts[0]);
    int minor = Integer.parseInt(parts[1]);

    if (major < 1 || minor < 0) {
      throw new IllegalArgumentException("Invalid version");
    }

    return new SchemaVersion(major, minor);
  }

  @JsonValue
  public String asString() {
    return major + "." + minor;
  }

  @Override
  public int compareTo(SchemaVersion o) {
    int c = Integer.compare(this.major, o.major);
    return c != 0 ? c : Integer.compare(this.minor, o.minor);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SchemaVersion that = (SchemaVersion) o;
    return major == that.major && minor == that.minor;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor);
  }

  @Override
  public String toString() {
    return "SchemaVersion{" +
        "major=" + major +
        ", minor=" + minor +
        '}';
  }
}