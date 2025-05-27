package yuhan.pro.mainserver.sharedkernel.jwt;


import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.sharedkernel.jwt.mapper.JwtMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) {
    Optional<Member> findMember = memberRepository.findByEmail(email);
    return findMember
        .map(JwtMapper::toCustomUserDetails)
        .orElseThrow(() -> new IllegalArgumentException("이상한 멤버")); // Todo: 커스텀 예외로 대체
  }
}
