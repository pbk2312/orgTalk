package yuhan.pro.mainserver.domain.member.dto;

import lombok.Builder;

@Builder
public record MemberResponse(
    String login,
    String avatarUrl,
    boolean authenticated
) {

}

