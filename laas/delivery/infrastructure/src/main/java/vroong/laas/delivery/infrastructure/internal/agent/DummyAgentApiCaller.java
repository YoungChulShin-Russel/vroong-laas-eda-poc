package vroong.laas.delivery.infrastructure.internal.agent;

import org.springframework.stereotype.Component;
import vroong.laas.delivery.core.domain.delivery.agent.AgentApiCaller;
import vroong.laas.delivery.core.domain.delivery.info.AgentInfo;

@Component
class DummyAgentApiCaller implements AgentApiCaller {

  @Override
  public AgentInfo getAgentInfo(Long agentId) {
    return new AgentInfo(
        agentId,
        "a25102700" + agentId,
        "테스트기사" + agentId,
        "0101234567" + agentId
    );
  }
}
