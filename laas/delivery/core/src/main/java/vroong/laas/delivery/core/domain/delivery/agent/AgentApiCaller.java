package vroong.laas.delivery.core.domain.delivery.agent;

import vroong.laas.delivery.core.domain.delivery.info.AgentInfo;

public interface AgentApiCaller {

  AgentInfo getAgentInfo(Long agentId);

}
