package vroong.laas.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.bff.client.DispatchServiceClient;

import java.util.Map;

/**
 * Dispatch Command Service
 * 배차 관련 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchCommandService {

    private final DispatchServiceClient dispatchServiceClient;

    /**
     * 기사 배차 제안 수락
     * POST /api/v1/dispatches/response
     */
    public Mono<Map<String, Object>> acceptDispatch(Map<String, Object> responseRequest) {
        log.info("Accepting dispatch");
        return dispatchServiceClient.acceptDispatch(responseRequest);
    }
}

