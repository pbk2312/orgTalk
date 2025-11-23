package yuhan.pro.mainserver.domain.auth.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;

/**
 * OAuth2 인증 처리를 담당하는 서비스 GitHub OAuth2를 통해 사용자 정보를 조회하고 Member를 생성/업데이트합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service extends DefaultOAuth2UserService {

    private final GithubApiClient githubApiClient;
    private final MemberOAuthHandler memberOAuthHandler;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("GitHub OAuth2 사용자 로딩 시작");

        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();

            String accessToken = userRequest.getAccessToken().getTokenValue();
            String email = githubApiClient.fetchPrimaryEmail(accessToken);

            log.info("OAuth2 사용자 정보 조회 완료: email={}", email);

            MemberDetails memberDetails = memberOAuthHandler.processOAuthUser(attributes, email);

            log.info("OAuth2 사용자 로딩 완료: memberId={}", memberDetails.getMemberId());
            return memberDetails;

        } catch (Exception e) {
            log.error("OAuth2 사용자 로딩 중 오류 발생", e);
            throw e;
        }
    }
}
