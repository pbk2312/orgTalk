package yuhan.pro.mainserver.domain.member.dto;

import lombok.Builder;

@Builder
public record MemberResponse(
    Long id,
    String login,
    String avatarUrl,
    boolean authenticated
) {

}

