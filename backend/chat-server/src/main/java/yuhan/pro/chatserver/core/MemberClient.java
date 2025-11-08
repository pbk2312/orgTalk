package yuhan.pro.chatserver.core;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import yuhan.pro.chatserver.domain.dto.ChatMemberResponse;
import yuhan.pro.chatserver.domain.dto.ChatMembersRequest;
import yuhan.pro.chatserver.domain.dto.MemberProfileUrlResponse;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberClient {

  private final WebClient webClient;
  private static final Duration TIMEOUT = Duration.ofSeconds(5);
  private static final MemberProfileUrlResponse DEFAULT_RESPONSE = new MemberProfileUrlResponse("");


  public Set<ChatMemberResponse> getChatMembers(Set<Long> memberIds, String jwtToken) {
    ChatMembersRequest request = new ChatMembersRequest(memberIds);

    return webClient.post()
        .uri("/api/member/chatMembers")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Set<ChatMemberResponse>>() {
        })
        .timeout(TIMEOUT)
        .retryWhen(Retry.backoff(1, Duration.ofMillis(100))
            .filter(throwable -> !(throwable instanceof WebClientResponseException)))
        .onErrorResume(throwable -> {
          log.error("Failed to get chat members: {}", throwable.getMessage());
          return Mono.empty();
        })
        .block();
  }

  public MemberProfileUrlResponse getMemberProfileUrl(Long memberId) {
    try {
      return webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/api/member/profile-url")
              .queryParam("memberId", memberId)
              .build())
          .retrieve()
          .bodyToMono(MemberProfileUrlResponse.class)
          .timeout(TIMEOUT)
          .retryWhen(Retry.backoff(1, Duration.ofMillis(100))
              .filter(throwable -> !(throwable instanceof WebClientResponseException)))
          .onErrorResume(throwable -> {
            log.warn("Failed to get member profile URL for memberId {}: {}", memberId, throwable.getMessage());
            return Mono.just(DEFAULT_RESPONSE);
          })
          .block();
    } catch (Exception e) {
      log.error("Unexpected error while getting member profile URL for memberId {}: {}", memberId, e.getMessage(), e);
      return DEFAULT_RESPONSE;
    }
  }
}
