package yuhan.pro.mainserver.domain.auth.service;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.REFRESH_TOKEN_BLACKLISTED;
import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.REFRESH_TOKEN_INVALID;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.CLAIM_MEMBER_ID;
import static yuhan.pro.mainserver.sharedkernel.common.constants.JwtConstants.REFRESH_TOKEN_COOKIE;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.security.jwt.JwtClaimsProvider;
import yuhan.pro.mainserver.sharedkernel.security.jwt.JwtValidator;
import yuhan.pro.mainserver.sharedkernel.security.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.security.jwt.dto.AccessTokenResponse;
import yuhan.pro.mainserver.sharedkernel.security.jwt.token.service.RefreshTokenService;
import yuhan.pro.mainserver.sharedkernel.security.jwt.blacklist.service.TokenBlacklistService;
import yuhan.pro.mainserver.sharedkernel.common.util.CookieUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final JwtValidator jwtValidator;
    private final JwtClaimsProvider claimsProvider;
    private final TokenBlacklistService blacklistService;
    private final RefreshTokenService refreshTokenService;

    public void logout(HttpServletResponse response, String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                Claims claims = claimsProvider.getClaims(refreshToken);
                Long memberId = claims.get(CLAIM_MEMBER_ID, Long.class);
                refreshTokenService.deleteRefreshToken(memberId);
            } catch (Exception e) {
                log.debug("Failed to parse refresh token during logout: {}", e.getMessage());
            }
        }

        CookieUtils.removeCookie(response, REFRESH_TOKEN_COOKIE);
        SecurityContextHolder.clearContext();
        blacklistService.blacklist(refreshToken);
        log.info("User logged out successfully");
    }

    public AccessTokenResponse getAccessToken(String refreshToken, HttpServletResponse response) {
        validateRefreshToken(refreshToken, response);
        return tokenProvider.getAccessToken(refreshToken);
    }

    private void validateRefreshToken(String refreshToken, HttpServletResponse response) {
        if (!jwtValidator.validate(refreshToken)) {
            CookieUtils.removeCookie(response, REFRESH_TOKEN_COOKIE);
            throw new CustomException(REFRESH_TOKEN_INVALID);
        }

        if (blacklistService.isBlacklisted(refreshToken)) {
            CookieUtils.removeCookie(response, REFRESH_TOKEN_COOKIE);
            throw new CustomException(REFRESH_TOKEN_BLACKLISTED);
        }
    }
}
