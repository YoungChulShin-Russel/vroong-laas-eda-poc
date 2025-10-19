package vroong.laas.dispatch.api.web.dispatch;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vroong.laas.dispatch.api.web.common.response.ApiResponse;
import vroong.laas.dispatch.api.web.dispatch.request.ProposeDispatchRequest;
import vroong.laas.dispatch.api.web.dispatch.request.RequestDispatchRequest;
import vroong.laas.dispatch.api.web.dispatch.request.RespondProposalRequest;
import vroong.laas.dispatch.api.web.dispatch.response.DispatchIdResponse;
import vroong.laas.dispatch.api.web.dispatch.response.ProposalIdResponse;
import vroong.laas.dispatch.core.application.dispatch.ProposeDispatchUseCase;
import vroong.laas.dispatch.core.application.dispatch.RequestDispatchUseCase;
import vroong.laas.dispatch.core.application.dispatch.RespondProposalUseCase;

@RestController
@RequestMapping("/api/v1/dispatches")
@RequiredArgsConstructor
public class DispatchController {

  private final RequestDispatchUseCase requestDispatchUseCase;
  private final ProposeDispatchUseCase proposeDispatchUseCase;
  private final RespondProposalUseCase respondProposalUseCase;

  @PostMapping("/request")
  public ApiResponse<DispatchIdResponse> requestDispatch(
      @RequestBody @Valid RequestDispatchRequest request) {
    Long dispatchId = requestDispatchUseCase.execute(request.toCommand());

    return ApiResponse.success(new DispatchIdResponse(dispatchId));
  }

  @PostMapping("/propose")
  public ApiResponse<ProposalIdResponse> proposeDispatch(
      @RequestBody @Valid ProposeDispatchRequest request) {
    Long proposalId = proposeDispatchUseCase.execute(request.toCommand());

    return ApiResponse.success(new ProposalIdResponse(proposalId));
  }

  @PostMapping("/respond")
  public ApiResponse<Void> respondDispatch(
      @RequestBody @Valid RespondProposalRequest request) {
    respondProposalUseCase.execute(request.toCommand());

    return ApiResponse.success();
  }
}
