package yuhan.pro.mainserver.sharedkernel.security.jwt;


import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.REFRESH_TOKEN_INVALID;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_EMAIL;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_MEMBER_ID;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_NAME;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_ROLE;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import yuhan.pro.mainserver.domain.member.dto.MemberDetails;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.security.jwt.dto.AccessTokenResponse;
import yuhan.pro.mainserver.sharedkernel.security.jwt.dto.TokenDto;
import yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants;
import yuhan.pro.mainserver.sharedkernel.security.jwt.token.service.RefreshTokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${custom.jwt.duration.access}")
    private Long accessTokenExpiration;

    @Value("${custom.jwt.duration.refresh}")
    private Long refreshTokenExpiration;

    private final SecretKey jwtSecretKey;
    private final RefreshTokenService refreshTokenService;

    public TokenDto generateTokenDto(Authentication authentication) {
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();

        String authority = extractAuthories(authentication);
        Long memberId = memberDetails.getMemberId();
        String email = memberDetails.getEmail();
        String name = memberDetails.getName();

        String accessToken = createAccessToken(email, name, authority, memberId);

        String refreshToken = createRefreshToken(email, name, memberId, authority);

        refreshTokenService.saveRefreshToken(memberId, refreshToken);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiration.intValue())
                .refreshTokenExpiresIn(refreshTokenExpiration.intValue())
                .build();
    }

    private String extractAuthories(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private String createToken(String email, String name, String authorities, Long memberId,
            Long expiration) {
        return Jwts.builder()
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLE, authorities)
                .claim(CLAIM_NAME, name)
                .claim(CLAIM_MEMBER_ID, memberId)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + expiration))
                .signWith(jwtSecretKey, SIG.HS256)
                .compact();
    }

    private String createAccessToken(String email, String name, String authorities, Long memberId) {
        return createToken(email, name, authorities, memberId, accessTokenExpiration);
    }

    private String createRefreshToken(String email, String name, Long memberId, String authorities) {
        return createToken(email, name, authorities, memberId, refreshTokenExpiration);
    }

    public AccessTokenResponse getAccessToken(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token).getPayload();

        String email = payload.get(CLAIM_EMAIL, String.class);
        String role = payload.get(CLAIM_ROLE, String.class);
        String name = payload.get(CLAIM_NAME, String.class);
        Long memberId = payload.get(CLAIM_MEMBER_ID, Long.class);

        boolean isValid = refreshTokenService.existsRefreshToken(memberId, token);
        if (!isValid) {
            log.warn("Refresh token not found in Redis for memberId: {}", memberId);
            throw new CustomException(REFRESH_TOKEN_INVALID);
        }

        String accessToken = createAccessToken(email, name, role, memberId);
        log.debug("Access token refreshed for memberId: {}", memberId);

        return AccessTokenResponse
                .builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiration.intValue())
                .build();
    }
}
