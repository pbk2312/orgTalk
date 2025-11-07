package yuhan.pro.mainserver.domain.auth.service;

import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_AVATAR_URL;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_ID;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_LOGIN;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_NAME;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.FIELD_EMAIL;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.FIELD_PRIMARY;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.FIELD_VERIFIED;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.GITHUB_EMAILS_URL;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.GITHUB_ORGS_URL;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.GITHUB_EMAIL_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;
import yuhan.pro.mainserver.domain.organization.dto.GithubOrgDto;
import yuhan.pro.mainserver.domain.organization.entity.Organization;
import yuhan.pro.mainserver.domain.organization.repository.OrganizationRepository;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            log.info("Loading OAuth2 user from GitHub...");
            OAuth2User oauth2User = super.loadUser(userRequest);
            Map<String, Object> attrs = oauth2User.getAttributes();
            log.info("OAuth2 user loaded successfully, attributes: {}", attrs.keySet());

        Long githubId = ((Number) attrs.get(ATTR_ID)).longValue();
        String login = (String) attrs.get(ATTR_LOGIN);
        String name = (String) attrs.get(ATTR_NAME);
        String avatarUrl = (String) attrs.get(ATTR_AVATAR_URL);

        String token = userRequest.getAccessToken().getTokenValue();

        String email = fetchPrimaryEmail(token);
        log.debug("OAuth2 user loaded - login: {}, email: {}", login, email);

        Member member = findMemberOrSave(githubId, login, name, email, avatarUrl);

        member.clearOrganizations();
        List<GithubOrgDto> orgDtos = fetchOrganizations(token);

        for (GithubOrgDto dto : orgDtos) {
            Organization org = organizationRepository.findById(dto.id())
                    .orElseGet(() -> organizationRepository.save(
                            Organization.builder()
                                    .id(dto.id())
                                    .login(dto.login())
                                    .avatarUrl(dto.avatarUrl())
                                    .build()
                    ));
            member.addOrganization(org);
        }

        Set<Long> orgIds = member.getOrganizations().stream()
                .map(Organization::getId)
                .collect(Collectors.toSet());

        log.debug("Member organizations loaded: {} organizations for memberId: {}",
                orgIds.size(), member.getId());

            return MemberDetails.builder()
                    .attributes(attrs)
                    .nickName(login)
                    .email(email)
                    .build()
                    .setMemberId(member.getId())
                    .setMemberRole(member.getMemberRole())
                    .setOrganizationIds(orgIds);
        } catch (Exception e) {
            log.error("Error loading OAuth2 user", e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Exception cause: {}", e.getCause().getMessage());
            }
            throw e;
        }
    }

    private String fetchPrimaryEmail(String token) {
        HttpEntity<Void> emailEntity = buildHttpEntity(token);
        ResponseEntity<List<Map<String, Object>>> emailsResponse = restTemplate.exchange(
                GITHUB_EMAILS_URL,
                HttpMethod.GET,
                emailEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> emails = Optional.ofNullable(emailsResponse.getBody())
                .orElse(Collections.emptyList());

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get(FIELD_PRIMARY))
                        && Boolean.TRUE.equals(e.get(FIELD_VERIFIED)))
                .map(e -> (String) e.get(FIELD_EMAIL))
                .findFirst()
                .orElseThrow(() -> new CustomException(GITHUB_EMAIL_NOT_FOUND));
    }

    private List<GithubOrgDto> fetchOrganizations(String token) {
        HttpEntity<Void> orgsEntity = buildHttpEntity(token);

        ResponseEntity<List<Map<String, Object>>> orgsResponse = restTemplate.exchange(
                GITHUB_ORGS_URL,
                HttpMethod.GET,
                orgsEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> memberships = Optional.ofNullable(orgsResponse.getBody())
                .orElse(Collections.emptyList());

        log.info("GitHub API Response Status: {}", orgsResponse.getStatusCode());
        log.info("Fetched {} organization memberships from GitHub", memberships.size());
        log.info("Raw memberships response: {}", memberships);

        // memberships API는 중첩된 organization 객체를 반환하므로 파싱 필요
        List<GithubOrgDto> organizations = memberships.stream()
                .filter(m -> {
                    String state = (String) m.get("state");
                    String role = (String) m.get("role");
                    log.debug("Organization membership - state: {}, role: {}", state, role);
                    return "active".equals(state); // active 상태만 포함
                })
                .map(m -> (Map<String, Object>) m.get("organization"))
                .filter(org -> org != null)
                .map(org -> {
                    GithubOrgDto dto = new GithubOrgDto(
                            ((Number) org.get("id")).longValue(),
                            (String) org.get("login"),
                            (String) org.get("avatar_url")
                    );
                    log.debug("Mapped organization: id={}, login={}", dto.id(), dto.login());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Successfully fetched {} active organizations", organizations.size());
        return organizations;
    }

    private Member findMemberOrSave(Long githubId, String login, String name,
            String email, String avatarUrl) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .githubId(githubId)
                                .memberRole(MemberRole.USER)
                                .login(login)
                                .name(name)
                                .email(email)
                                .avatarUrl(avatarUrl)
                                .build()
                ));
    }

    private HttpEntity<Void> buildHttpEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }
}



