package yuhan.pro.mainserver.sharedkernel.security.authentication;

import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_EMAIL;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_MEMBER_ID;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_NAME;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_ORG_IDS;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_ROLE;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.DEFAULT_PASSWORD;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.ORG_ROLE_PREFIX;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.ROLE_PREFIX;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.INVALID_JWT_CLAIMS;

import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.domain.member.entity.MemberRole;
import yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.security.jwt.JwtClaimsProvider;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

    private final JwtClaimsProvider claimsProvider;

    public Authentication getAuthentication(String accessToken) {
        Claims claims = claimsProvider.getClaims(accessToken);
        validateCondition(claims == null);

        UserDetails userDetails = buildUserDetails(claims);
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(userDetails, accessToken, authorities);
    }

    private UserDetails buildUserDetails(Claims claims) {
        String email = claims.get(CLAIM_EMAIL, String.class);
        String name = claims.get(CLAIM_NAME, String.class);
        Long memberId = claims.get(CLAIM_MEMBER_ID, Long.class);
        String roleStr = claims.get(CLAIM_ROLE, String.class);

        validateCondition(email == null || email.isBlank());
        validateCondition(roleStr == null || roleStr.isBlank());

        Set<Long> organizationIds = extractOrganizationIds(claims);

        return CustomUserDetails.builder()
                .memberId(memberId)
                .username(email)
                .nickName(name)
                .memberRole(MemberRole.valueOf(roleStr))
                .organizationIds(organizationIds)
                .password(DEFAULT_PASSWORD)
                .build();
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        String role = claims.get(CLAIM_ROLE, String.class);
        if (role == null || role.isBlank()) {
            return List.of();
        }

        Set<Long> orgIds = extractOrganizationIds(claims);

        return orgIds.stream()
                .map(id -> new SimpleGrantedAuthority(ORG_ROLE_PREFIX + id))
                .collect(Collectors.toCollection(() -> {
                    List<GrantedAuthority> list = List.of(
                            new SimpleGrantedAuthority(ROLE_PREFIX + role));
                    return new java.util.ArrayList<>(list);
                }));
    }

    private Set<Long> extractOrganizationIds(Claims claims) {
        @SuppressWarnings("unchecked")
        List<Integer> orgIdsFromToken = claims.get(CLAIM_ORG_IDS, List.class);
        return JwtConstants.convertOrgIdsToSet(orgIdsFromToken);
    }

    private static void validateCondition(boolean invalid) {
        if (invalid) {
            throw new CustomException(INVALID_JWT_CLAIMS);
        }
    }
}
