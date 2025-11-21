package yuhan.pro.mainserver.domain.auth.service;

import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_AVATAR_URL;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_ID;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_LOGIN;
import static yuhan.pro.mainserver.domain.auth.constants.OAuth2Constants.ATTR_NAME;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
import yuhan.pro.mainserver.domain.member.entity.Member;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;
import yuhan.pro.mainserver.domain.member.repository.MemberRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberOAuthHandler {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDetails processOAuthUser(Map<String, Object> attributes, String email) {
        Long githubId = extractGithubId(attributes);
        String login = extractLogin(attributes);
        String name = extractName(attributes);
        String avatarUrl = extractAvatarUrl(attributes);

        log.info("OAuth2 사용자 처리 시작: login={}, email={}", login, email);

        Member member = findOrCreateMember(githubId, login, name, email, avatarUrl);
        log.info("Member 처리 완료: memberId={}", member.getId());

        return buildMemberDetails(attributes, member, email);
    }

    private Long extractGithubId(Map<String, Object> attributes) {
        return ((Number) attributes.get(ATTR_ID)).longValue();
    }

    private String extractLogin(Map<String, Object> attributes) {
        return (String) attributes.get(ATTR_LOGIN);
    }

    private String extractName(Map<String, Object> attributes) {
        return (String) attributes.get(ATTR_NAME);
    }

    private String extractAvatarUrl(Map<String, Object> attributes) {
        return (String) attributes.get(ATTR_AVATAR_URL);
    }

    private Member findOrCreateMember(Long githubId, String login, String name,
            String email, String avatarUrl) {
        // 람다 대신 메서드 참조 사용
        return memberRepository.findByEmail(email)
                .map(this::updateMemberIfNeeded)
                .orElseGet(() -> createNewMember(githubId, login, name, email, avatarUrl));
    }

    private Member updateMemberIfNeeded(Member member) {
        log.debug("기존 Member 사용: memberId={}", member.getId());
        return member;
    }

    private Member createNewMember(Long githubId, String login, String name,
            String email, String avatarUrl) {
        Member newMember = Member.builder()
                .githubId(githubId)
                .memberRole(MemberRole.USER)
                .login(login)
                .name(name)
                .email(email)
                .avatarUrl(avatarUrl)
                .build();

        Member saved = memberRepository.save(newMember);
        log.info("새로운 Member 생성: memberId={}, login={}", saved.getId(), login);
        return saved;
    }

    private MemberDetails buildMemberDetails(Map<String, Object> attributes, Member member,
            String email) {
        return MemberDetails.builder()
                .attributes(attributes)
                .nickName(member.getLogin())
                .email(email)
                .build()
                .setMemberId(member.getId())
                .setMemberRole(member.getMemberRole());
    }
}
