package yuhan.pro.mainserver.domain.member.service;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.mainserver.domain.member.dto.ChatMemberResponse;
import yuhan.pro.mainserver.domain.member.dto.ChatMembersRequest;
import yuhan.pro.mainserver.domain.member.dto.MemberResponse;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.mapper.MemberMapper;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;
import yuhan.pro.mainserver.domain.organization.entity.Organization;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.dto.PageResponse;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final OrganizationRepository organizationRepository;

  // Todo: 캐시 처리 고민(채팅 서버랑 큰 상호작용을 안할거고 메인 서버 용도로만 할건데 Redis 말고 카페인 캐시 고민)
  // Todo: 조직에 해당하지 않은 멤버는 401이나 403 예외 처리


  @Transactional(readOnly = true)
  public PageResponse<OrganizationsResponse> getOrganizations(int page, int size) {
    String email = getEmail();
    Member findMember = findMemberOrThrow(email);

    PageRequest pageRequest = PageRequest.of(page, size);
    Page<Organization> organizationPage = organizationRepository.findByMember(findMember,
        pageRequest);

    Page<OrganizationsResponse> dtoPage = organizationPage.map(
        MemberMapper::toOrganizationResponse);

    return PageResponse.fromPage(dtoPage);
  }


  // Todo: 캐시 처리 고민
  @Transactional(readOnly = true)
  public MemberResponse isLogin() {
    String email = getEmail();
    if (email == null) {
      log.info("Member is not logged in");
      return MemberMapper.unauthenticatedResponse();
    }
    Member member = findMemberOrThrow(email);
    log.info("Member {} is logged in", member.getEmail());
    return MemberMapper.toMemberResponse(member);
  }

  @Transactional(readOnly = true)
  public Set<ChatMemberResponse> getChatMembers(ChatMembersRequest request) {

    Set<Member> findMembers = findChatMembers(request.memberIds());

    return findMembers.stream()
        .map(MemberMapper::toChatMemberResponse)
        .collect(Collectors.toSet());
  }

  private Set<Member> findChatMembers(Set<Long> memberIds) {
    return memberRepository.findAllByIdIn(memberIds);
  }

  private Member findMemberOrThrow(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private String getEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return auth.getName();
  }
}
