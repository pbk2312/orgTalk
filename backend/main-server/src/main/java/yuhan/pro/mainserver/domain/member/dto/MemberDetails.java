package yuhan.pro.mainserver.domain.member.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;

@Getter
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDetails implements OAuth2User {

  @Setter
  private Long memberId;

  private String name;
  private String email;

  @Setter
  private Map<String, Object> attributes;

  @Setter
  private MemberRole memberRole;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(memberRole.name()));
  }

  public static MemberDetails from(Member member) {
    MemberDetails memberDetails = new MemberDetails();
    memberDetails.name = member.getName();
    memberDetails.email = member.getEmail();
    return memberDetails;
  }

  @Builder
  public MemberDetails(Map<String, Object> attributes,
      String nickName,
      String email
  ) {
    this.attributes = attributes;
    this.name = nickName;
    this.email = email;
  }
}
