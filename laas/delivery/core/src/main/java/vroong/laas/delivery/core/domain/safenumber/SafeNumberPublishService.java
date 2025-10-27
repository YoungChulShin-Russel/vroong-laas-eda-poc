package vroong.laas.delivery.core.domain.safenumber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeNumberPublishService {

  private final SafeNumberApiClient safeNumberApiClient;

  public SafeNumber publish(String originalNumber) {
    try {
      // DB 저장 필요
      return safeNumberApiClient.publish(originalNumber);
    } catch (Exception e) {
      log.error("안심번호 발급 실패. 입력 번호: {}", originalNumber, e);
      return null;
    }
  }

}
