package yuhan.pro.mainserver.domain.member.service;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.GITHUB_EMAIL_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private static final String ORGS_URL = "https://api.github.com/user/orgs";
  private static final String EMAILS_URL = "https://api.github.com/user/emails";

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User oauth2User = super.loadUser(userRequest);
    Map<String, Object> attrs = oauth2User.getAttributes();

    Long githubId = ((Number) attrs.get("id")).longValue();
    String login = (String) attrs.get("login");
    String name = (String) attrs.get("name");
    String avatarUrl = (String) attrs.get("avatar_url");

    String token = userRequest.getAccessToken().getTokenValue();

    HttpEntity<Void> emailEntity = buildHttpEntity(token);
    ResponseEntity<List<Map<String, Object>>> emailsResponse = restTemplate.exchange(
        EMAILS_URL,
        HttpMethod.GET,
        emailEntity,
        new ParameterizedTypeReference<>() {
        }
    );

    List<Map<String, Object>> emails = Optional.ofNullable(emailsResponse.getBody())
        .orElse(Collections.emptyList());

    String email = emails.stream()
        .filter(e -> Boolean.TRUE.equals(e.get("primary"))
            && Boolean.TRUE.equals(e.get("verified")))
        .map(e -> (String) e.get("email"))
        .findFirst()
        .orElseThrow(() -> new CustomException(GITHUB_EMAIL_NOT_FOUND));

    log.info("Github ID: {}, Login: {}, Name: {}, Email: {}, Avatar URL: {}",
        githubId, login, name, email, avatarUrl);

    Member member = findMemberOrSave(githubId, login, name, email, avatarUrl);

    member.clearOrganizations();
    HttpEntity<Void> orgsEntity = buildHttpEntity(token);
    ResponseEntity<List<GithubOrgDto>> orgsResponse = restTemplate.exchange(
        ORGS_URL,
        HttpMethod.GET,
        orgsEntity,
        new ParameterizedTypeReference<>() {
        }
    );

    List<GithubOrgDto> orgDtos = Optional.ofNullable(orgsResponse.getBody())
        .orElse(Collections.emptyList());

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

    return MemberDetails.builder()
        .attributes(attrs)
        .nickName(login)
        .email(email)
        .build()
        .setMemberId(member.getId())
        .setMemberRole(member.getMemberRole());
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

  private static HttpEntity<Void> buildHttpEntity(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    return new HttpEntity<>(headers);
  }
}
