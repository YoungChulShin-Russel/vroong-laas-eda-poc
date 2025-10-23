package vroong.laas.bff.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.bff.dto.ApiResponse;
import vroong.laas.bff.service.DispatchCommandService;

import java.util.Map;

/**
 * Dispatch Command Controller
 * 배차 관련 명령 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/commands/dispatches")
@RequiredArgsConstructor
public class DispatchCommandController {

    private final DispatchCommandService dispatchCommandService;

    /**
     * 기사 배차 제안 수락
     * POST /api/v1/commands/dispatches/response
     */
    @PostMapping("/response")
    public Mono<ApiResponse<Map<String, Object>>> acceptDispatch(@RequestBody Map<String, Object> responseRequest) {
        log.info("POST /api/v1/commands/dispatches/response");
        return dispatchCommandService.acceptDispatch(responseRequest)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to accept dispatch: {}", e.getMessage()));
    }
}

