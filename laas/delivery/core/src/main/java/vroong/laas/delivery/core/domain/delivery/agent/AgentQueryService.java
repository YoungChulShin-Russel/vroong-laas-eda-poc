package vroong.laas.delivery.core.domain.delivery.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.delivery.core.domain.delivery.info.AgentInfo;

@Service
@RequiredArgsConstructor
public class AgentQueryService {

  private final AgentApiCaller agentApiCaller;

  public AgentInfo getAgentInfo(Long agentId) {
    return agentApiCaller.getAgentInfo(agentId);
  }
}
