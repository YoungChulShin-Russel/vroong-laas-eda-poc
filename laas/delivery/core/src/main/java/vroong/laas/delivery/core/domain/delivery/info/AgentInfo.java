package vroong.laas.delivery.core.domain.delivery.info;

import vroong.laas.delivery.core.domain.safenumber.SafeNumber;

public record AgentInfo(
    Long id,
    String agentNumber,
    String agentName,
    String phoneNumber
) {
  public AgentInfo withSafeNumber(SafeNumber safeNumber) {
    if (safeNumber == null) {
      return this;
    }
    return new AgentInfo(id, agentNumber, agentName, safeNumber.value());
  }
}
