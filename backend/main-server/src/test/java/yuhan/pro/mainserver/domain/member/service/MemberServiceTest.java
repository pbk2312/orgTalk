package yuhan.pro.mainserver.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;

import java.util.Collections;
import java.util.Optional;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;
import yuhan.pro.mainserver.domain.organization.entity.Organization;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private MemberService memberService;

  @BeforeEach
  void setUpAuthentication() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList());
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
    @DisplayName("성공")
    void returnsOrganizationDtoSetWhenMemberExists() {
      // given
      String email = "test@example.com";

      Organization org1 = Organization.builder()
          .id(1L)
          .login("org-login-A")
          .avatarUrl("http://avatar-A.url")
          .build();
      Organization org2 = Organization.builder()
          .id(2L)
          .login("org-login-B")
          .avatarUrl("http://avatar-B.url")
          .build();

      Member mockMember = Member.builder()
          .login("loginA")
          .name("nameA")
          .email(email)
          .avatarUrl("http://avatar.url")
          .memberRole(MemberRole.USER)
          .githubId(123L)
          .build();
      mockMember.setOrganizations(Set.of(org1, org2));

      when(memberRepository.findByEmail(email))
          .thenReturn(Optional.of(mockMember));

      // when
      Set<OrganizationsResponse> result = memberService.getOrganizations();

      // then
      assertThat(result)
          .hasSize(2)
          .extracting(OrganizationsResponse::id)
          .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("실패")
    void failsWhenMemberDoesNotExist() {
      // given
      String email = "test@example.com";
      when(memberRepository.findByEmail(email))
          .thenReturn(Optional.empty());

      // when / then
      assertThatThrownBy(() -> memberService.getOrganizations())
          .isInstanceOf(CustomException.class)
          .satisfies(ex -> {
            CustomException ce = (CustomException) ex;
            assertThat(ce.getExceptionCode()).isEqualTo(MEMBER_NOT_FOUND);
            assertThat(ce.getMessage()).isEqualTo("존재하지 않는 회원입니다.");
          });
    }
  }
}
