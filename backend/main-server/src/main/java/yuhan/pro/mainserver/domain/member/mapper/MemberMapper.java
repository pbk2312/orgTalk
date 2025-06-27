package yuhan.pro.mainserver.domain.member.mapper;


import yuhan.pro.mainserver.domain.member.dto.ChatMemberResponse;
import yuhan.pro.mainserver.domain.member.dto.MemberResponse;
import yuhan.pro.mainserver.domain.member.entity.Member;

public class MemberMapper {

  private MemberMapper() {
  }

  public static MemberResponse toMemberResponse(Member member) {
    return MemberResponse.builder()
        .id(member.getId())
        .login(member.getLogin())
        .avatarUrl(member.getAvatarUrl())
        .authenticated(true)
        .build();
  }

  public static MemberResponse unauthenticatedResponse() {
    return MemberResponse.builder()
        .login(null)
        .avatarUrl(null)
        .authenticated(false)
        .build();
  }

  public static ChatMemberResponse toChatMemberResponse(Member member) {
    return new ChatMemberResponse(
        member.getId(),
        member.getLogin(),
        member.getAvatarUrl()
    );
  }
}
