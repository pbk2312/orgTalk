package yuhan.pro.mainserver.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import yuhan.pro.mainserver.sharedkernel.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private MemberService memberService;

  private static final long TEST_MEMBER_ID = 1L;
  private static final int PAGE = 1;
  private static final int SIZE = 5;

  @BeforeEach
  void setUpAuthentication() {
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .memberId(TEST_MEMBER_ID)
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
    void returnsPageResponseWhenOrganizationsExist() {
      // given
      OrganizationsResponse orgA = new OrganizationsResponse(1L, "avatarA", "orgA");
      OrganizationsResponse orgB = new OrganizationsResponse(2L, "avatarB", "orgB");
      OrganizationsResponse orgC = new OrganizationsResponse(3L, "avatarC", "orgC");
      Page<OrganizationsResponse> page = new PageImpl<>(List.of(orgA, orgB, orgC),
          PageRequest.of(PAGE, SIZE), 3);

      when(organizationRepository.findByMemberIdProjected(
          eq(TEST_MEMBER_ID), eq(PageRequest.of(PAGE, SIZE))))
          .thenReturn(page);

      // when
      PageResponse<OrganizationsResponse> response = memberService.getOrganizations(PAGE, SIZE);

      // then
      assertThat(response.getContent())
          .hasSize(3)
          .extracting(OrganizationsResponse::id)
          .containsExactlyInAnyOrder(1L, 2L, 3L);
      assertThat(response.getPage()).isEqualTo(PAGE);
      assertThat(response.getSize()).isEqualTo(SIZE);
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
  }
}
