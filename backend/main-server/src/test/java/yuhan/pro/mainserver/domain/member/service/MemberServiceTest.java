package yuhan.pro.mainserver.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import yuhan.pro.mainserver.domain.member.dto.MemberResponse;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.dto.PageResponse;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private MemberService memberService;


  @BeforeEach
  void setUp() {
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .memberId(1L)
        .memberRole(MemberRole.USER)
        .organizationIds(Set.of(1L, 2L, 3L))
        .password("password")
        .build();
    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("멤버에 속한 조직 가져오기")
  class GetOrganizationsTests {

    @Test
    @DisplayName("성공 시 PageResponse 반환")
    void getOrganizationsSuccess() {
      // given
      OrganizationsResponse orgA = new OrganizationsResponse(1L, "avatarA", "orgA");
      OrganizationsResponse orgB = new OrganizationsResponse(2L, "avatarB", "orgB");
      OrganizationsResponse orgC = new OrganizationsResponse(3L, "avatarC", "orgC");
      Page<OrganizationsResponse> page = new PageImpl<>(List.of(orgA, orgB, orgC),
          PageRequest.of(0, 1), 3);

      when(organizationRepository.findByMemberIdProjected(
          eq(1L), eq(PageRequest.of(0, 1))))
          .thenReturn(page);

      // when
      PageResponse<OrganizationsResponse> response = memberService.getOrganizations(0, 1);

      // then
      assertThat(response.getContent())
          .hasSize(3)
          .extracting(OrganizationsResponse::id)
          .containsExactlyInAnyOrder(1L, 2L, 3L);
      assertThat(response.getPage()).isEqualTo(0);
      assertThat(response.getSize()).isEqualTo(1);
      assertThat(response.getTotalElements()).isEqualTo(3);
      assertThat(response.getTotalPages()).isEqualTo(3);
      assertThat(response.getContent().getFirst().id()).isEqualTo(1L);
      assertThat(response.getContent().getLast().id()).isEqualTo(3L);
    }
  }

  @Nested
  @DisplayName("로그인 상태 검증")
  class IsLoginTests {

    @Test
    @DisplayName("로그인 상태 검증 성공")
    void isLoginSuccess() {
      // given
      Member member = Member.builder()
          .login("pbk2312")
          .memberRole(MemberRole.USER)
          .avatarUrl("kkkkkk@aaaa")
          .name("pbk2312")
          .githubId(1L)
          .email("pbk2312@inu.ac.kr")
          .build();

      when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));

      // when
      MemberResponse response = memberService.isLogin();

      // then
      assertThat(response.login()).isEqualTo("pbk2312");
      assertThat(response.authenticated()).isTrue();
    }


    @Test
    @DisplayName("로그인하지 않은 상태이면 unauthenticatedResponse 반환")
    void isLoginUnauthenticated() {
      // given
      SecurityContextHolder.clearContext();

      // when
      MemberResponse response = memberService.isLogin();

      // then
      assertThat(response.authenticated()).isFalse();
      assertThat(response.id()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 회원일 시, 예외 처리")
    void isLoginFail() {
      // given
      when(memberRepository.findById(1L)).thenReturn(java.util.Optional.empty());

      // when & then
      assertThatThrownBy(() -> memberService.isLogin())
          .isInstanceOf(CustomException.class)
          .hasMessageContaining("존재하지 않는 회원입니다.");
    }
  }
}

