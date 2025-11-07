package yuhan.pro.mainserver.domain.organization.service;


import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.MEMBER_NOT_FOUND;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.ORGANIZATION_NOT_FOUND;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.domain.organization.dto.CreateOrganizationRequest;
import yuhan.pro.mainserver.domain.organization.dto.CreateOrganizationResponse;
import yuhan.pro.mainserver.domain.organization.dto.OrganizationsInfoResponse;
import yuhan.pro.mainserver.domain.organization.entity.Organization;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.security.authentication.CustomUserDetails;
import yuhan.pro.mainserver.sharedkernel.security.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.security.jwt.dto.TokenDto;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final MemberRepository memberRepository;
  private final TokenProvider tokenProvider;
  private final SecureRandom secureRandom = new SecureRandom();

  public OrganizationsInfoResponse getOrganizationInfo(Long organizationId) {
    return findOrganizationWithMemberCountOrThrow(organizationId);
  }

  @Transactional
  public CreateOrganizationResponse createOrganization(CreateOrganizationRequest request) {
    Long memberId = getMemberId();
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

    // 임의의 ID 생성 (GitHub ID와 충돌하지 않도록 큰 범위 사용)
    Long generatedId = generateUniqueOrganizationId();

    Organization organization = Organization.builder()
        .id(generatedId)
        .login(request.login())
        .avatarUrl(request.avatarUrl() != null ? request.avatarUrl() : generateDefaultAvatarUrl())
        .members(new HashSet<>()) // 빈 Set으로 초기화
        .build();

    // 조직 먼저 저장
    organizationRepository.save(organization);
    organizationRepository.flush(); // 즉시 DB에 반영

    // 생성한 사용자를 조직에 추가
    member.addOrganization(organization);
    memberRepository.save(member);
    memberRepository.flush(); // 즉시 DB에 반영

    // 업데이트된 조직 목록으로 새로운 JWT 토큰 생성
    Set<Long> updatedOrgIds = member.getOrganizations().stream()
        .map(Organization::getId)
        .collect(Collectors.toSet());

    // 새로운 Authentication 객체 생성
    MemberDetails memberDetails = MemberDetails.builder()
        .email(member.getEmail())
        .nickName(member.getLogin())
        .build()
        .setMemberId(member.getId())
        .setMemberRole(member.getMemberRole())
        .setOrganizationIds(updatedOrgIds);

    Authentication newAuth = new UsernamePasswordAuthenticationToken(
        memberDetails,
        null,
        memberDetails.getAuthorities()
    );

    // 새로운 토큰 생성
    TokenDto tokenDto = tokenProvider.generateTokenDto(newAuth);

    return CreateOrganizationResponse.builder()
        .id(organization.getId())
        .login(organization.getLogin())
        .avatarUrl(organization.getAvatarUrl())
        .accessToken(tokenDto.accessToken())
        .refreshToken(tokenDto.refreshToken())
        .accessTokenExpiresIn(tokenDto.accessTokenExpiresIn())
        .refreshTokenExpiresIn(tokenDto.refreshTokenExpiresIn())
        .build();
  }

  private OrganizationsInfoResponse findOrganizationWithMemberCountOrThrow(Long organizationId) {
    return organizationRepository
        .findInfoWithMemberCount(organizationId)
        .orElseThrow(() -> new CustomException(ORGANIZATION_NOT_FOUND));
  }

  private Long getMemberId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null
        || !auth.isAuthenticated()
        || !(auth instanceof UsernamePasswordAuthenticationToken)) {
      throw new CustomException(MEMBER_NOT_FOUND);
    }

    Object principal = auth.getPrincipal();
    if (!(principal instanceof CustomUserDetails)) {
      throw new CustomException(MEMBER_NOT_FOUND);
    }

    return ((CustomUserDetails) principal).getMemberId();
  }

  private Long generateUniqueOrganizationId() {
    Long id;
    do {
      // 1억부터 10억 사이의 랜덤 ID 생성 (GitHub Organization ID와 충돌 방지)
      id = 100_000_000L + (long) (secureRandom.nextDouble() * 900_000_000L);
    } while (organizationRepository.existsById(id));
    return id;
  }

  private String generateDefaultAvatarUrl() {
    // 기본 아바타 URL (GitHub 스타일의 identicon 또는 기본 이미지)
    return "https://avatars.githubusercontent.com/u/0?v=4";
  }
}
