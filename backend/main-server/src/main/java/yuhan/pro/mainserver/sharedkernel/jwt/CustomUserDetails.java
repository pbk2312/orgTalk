package yuhan.pro.mainserver.sharedkernel.jwt;

import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomUserDetails implements UserDetails {

  private Long memberId;
  private String username;
  private String nickName;
  private String password;
  private MemberRole memberRole;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(memberRole.toString()));
  }

  @Builder
  public CustomUserDetails(Long memberId, String username, String nickName, String password,
      MemberRole memberRole) {
    this.memberId = memberId;
    this.username = username;
    this.nickName = nickName;
    this.password = password;
    this.memberRole = memberRole;
  }
}
