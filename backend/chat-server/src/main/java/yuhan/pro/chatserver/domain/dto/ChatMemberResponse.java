package yuhan.pro.chatserver.domain.dto;

public record ChatMemberResponse(
    Long id,
    String login,
    String avatarUrl
) {

}
