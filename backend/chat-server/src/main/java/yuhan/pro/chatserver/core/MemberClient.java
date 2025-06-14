package yuhan.pro.chatserver.core;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import yuhan.pro.chatserver.domain.dto.ChatMemberResponse;
import yuhan.pro.chatserver.domain.dto.ChatMembersRequest;
import yuhan.pro.chatserver.domain.dto.MemberProfileUrlResponse;

@RequiredArgsConstructor
@Component
public class MemberClient {

  private final WebClient webClient;


  public Set<ChatMemberResponse> getChatMembers(Set<Long> memberIds, String jwtToken) {
    ChatMembersRequest request = new ChatMembersRequest(memberIds);

    return webClient.post()
        .uri("/api/member/chatMembers")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Set<ChatMemberResponse>>() {
        })
        .block();
  }

  public MemberProfileUrlResponse getMemberProfileUrl(Long memberId) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/member/profile-url")
            .queryParam("memberId", memberId)
            .build())
        .retrieve()
        .bodyToMono(MemberProfileUrlResponse.class)
        .block();
  }
}
