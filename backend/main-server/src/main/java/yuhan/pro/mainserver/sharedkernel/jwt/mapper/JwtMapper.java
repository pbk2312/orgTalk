package yuhan.pro.mainserver.sharedkernel.jwt.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.organization.entity.Organization;
import yuhan.pro.mainserver.sharedkernel.jwt.CustomUserDetails;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.TokenDto;

public class JwtMapper {

  private JwtMapper() {
  }

  public static TokenDto toDto(String accessToken, String refreshToken, int accessTokenExpiresIn,
      int refreshTokenExpiresIn) {
    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(accessTokenExpiresIn)
        .refreshTokenExpiresIn(refreshTokenExpiresIn)
        .build();
  }

  public static CustomUserDetails toCustomUserDetails(Member member) {
    Set<Long> orgIds = member.getOrganizations().stream()
        .map(Organization::getId)
        .collect(Collectors.toSet());

    return CustomUserDetails.builder()
        .nickName(member.getName())
        .password("SOCIAL_LOGIN_USER") // 비밀번호는 소셜 로그인용 임시값
        .memberRole(member.getMemberRole())
        .memberId(member.getId())
        .username(member.getEmail())
        .organizationIds(orgIds)
        .build();
  }
}
