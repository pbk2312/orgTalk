package yuhan.pro.mainserver.domain.auth.service;

import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.FIELD_EMAIL;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.FIELD_PRIMARY;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.FIELD_VERIFIED;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.GITHUB_EMAILS_URL;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubApiClient {

    private final RestTemplate restTemplate;

    public String fetchPrimaryEmail(String accessToken) {
        log.debug("GitHub에서 Primary Email 조회 시작");

        HttpEntity<Void> requestEntity = buildHttpEntity(accessToken);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                GITHUB_EMAILS_URL,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> emails = Optional.ofNullable(response.getBody())
                .orElse(Collections.emptyList());

        String primaryEmail = emails.stream()
                .filter(this::isPrimaryAndVerified)
                .map(email -> (String) email.get(FIELD_EMAIL))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("GitHub에서 Primary Email을 찾을 수 없습니다. 조회된 이메일 수: {}", emails.size());
                    return new CustomException(GITHUB_EMAIL_NOT_FOUND);
                });

        log.debug("Primary Email 조회 완료: {}", primaryEmail);
        return primaryEmail;
    }

    private boolean isPrimaryAndVerified(Map<String, Object> email) {
        return Boolean.TRUE.equals(email.get(FIELD_PRIMARY))
                && Boolean.TRUE.equals(email.get(FIELD_VERIFIED));
    }

    private HttpEntity<Void> buildHttpEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }
}



