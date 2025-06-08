package yuhan.pro.mainserver.domain.member.mapper;


import java.util.Set;
import java.util.stream.Collectors;
import yuhan.pro.mainserver.domain.member.dto.ChatMemberResponse;
import yuhan.pro.mainserver.domain.member.dto.MemberResponse;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;
import yuhan.pro.mainserver.domain.organization.entity.Organization;

public class MemberMapper {

  private MemberMapper() {
  }

  public static OrganizationsResponse toOrganizationResponse(Organization organization) {
    return OrganizationsResponse.builder()
        .id(organization.getId())
        .login(organization.getLogin())
        .avatarUrl(organization.getAvatarUrl())
        .build();
  }


  public static Set<OrganizationsResponse> toOrganizationsResponse(
      Set<Organization> organizations
  ) {
    return organizations.stream()
        .map(org -> OrganizationsResponse.builder()
            .id(org.getId())
            .login(org.getLogin())
            .avatarUrl(org.getAvatarUrl())
            .build(
            ))
        .collect(Collectors.toSet());
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
