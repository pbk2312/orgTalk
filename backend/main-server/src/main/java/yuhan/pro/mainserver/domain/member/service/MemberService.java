package yuhan.pro.mainserver.domain.member.service;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.mainserver.domain.member.dto.ChatMemberResponse;
import yuhan.pro.mainserver.domain.member.dto.ChatMembersRequest;
import yuhan.pro.mainserver.domain.member.dto.MemberProfileUrlResponse;
import yuhan.pro.mainserver.domain.member.dto.MemberResponse;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.mapper.MemberMapper;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsResponse;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.dto.PageResponse;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.jwt.CustomUserDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final OrganizationRepository organizationRepository;


  @Transactional(readOnly = true)
  public PageResponse<OrganizationsResponse> getOrganizations(int page, int size) {
    Long memberId = getMemberId();

    PageRequest pageRequest = PageRequest.of(page, size);
    Page<OrganizationsResponse> dtoPage =
        organizationRepository.findByMemberIdProjected(memberId, pageRequest);

    return PageResponse.fromPage(dtoPage);
  }

  // Todo: 캐시 처리 고민
  @Transactional(readOnly = true)
  public MemberResponse isLogin() {
    Long memberId = getMemberId();
    if (memberId == null) {
      return MemberMapper.unauthenticatedResponse();
    }

    Member member = findMemberOrThrow(memberId);

    return MemberMapper.toMemberResponse(member);
  }

  @Transactional(readOnly = true)
  public Set<ChatMemberResponse> getChatMembers(ChatMembersRequest request) {

    Set<Member> findMembers = findChatMembers(request.memberIds());

    return findMembers.stream()
        .map(MemberMapper::toChatMemberResponse)
        .collect(Collectors.toSet());
  }

  @Transactional(readOnly = true)
  public MemberProfileUrlResponse getMemberProfileUrl(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    return new MemberProfileUrlResponse(member.getAvatarUrl());
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }

  private Set<Member> findChatMembers(Set<Long> memberIds) {
    return memberRepository.findAllByIdIn(memberIds);
  }

  private Long getMemberId() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null
        || !auth.isAuthenticated()
        || !(auth instanceof UsernamePasswordAuthenticationToken)) {
      return null;
    }

    Object principal = auth.getPrincipal();
    if (!(principal instanceof CustomUserDetails)) {
      return null;
    }

    return ((CustomUserDetails) principal).getMemberId();
  }
}
