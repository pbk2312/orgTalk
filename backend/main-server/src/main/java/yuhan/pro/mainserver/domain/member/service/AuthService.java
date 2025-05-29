package yuhan.pro.mainserver.domain.member.service;

import static yuhan.pro.mainserver.sharedkernel.exception.ExceptionCode.REFRESH_TOKEN_BLACKLISTED;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import yuhan.pro.mainserver.sharedkernel.exception.CustomException;
import yuhan.pro.mainserver.sharedkernel.jwt.JwtValidator;
import yuhan.pro.mainserver.sharedkernel.jwt.TokenBlacklistService;
import yuhan.pro.mainserver.sharedkernel.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.AccessTokenResponse;
import yuhan.pro.mainserver.sharedkernel.util.CookieUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final TokenProvider tokenProvider;
  private final JwtValidator jwtValidator;
  private final TokenBlacklistService blacklistService;

  public void logout(HttpServletResponse response, String refreshToken) {
    CookieUtils.removeCookie(response, "refreshToken");
    SecurityContextHolder.clearContext();
    blacklistService.blacklist(refreshToken);
    log.info("Logout successfully");
  }

  public AccessTokenResponse getAccessToken(String refreshToken,
      HttpServletResponse response) {
    validateRefreshToken(refreshToken, response);
    return tokenProvider.getAccessToken(refreshToken);
  }

  private void validateRefreshToken(String refreshToken, HttpServletResponse response) {
    if (!jwtValidator.validate(refreshToken)) {
      log.warn("Invalid refresh token : {}", refreshToken);
      CookieUtils.removeCookie(response, "refreshToken");
      throw new CustomException(REFRESH_TOKEN_BLACKLISTED);
    }

    if (blacklistService.isBlacklisted(refreshToken)) {
      log.warn("Blacklisted refresh token: {}", refreshToken);
      CookieUtils.removeCookie(response, "refreshToken");
      throw new CustomException(REFRESH_TOKEN_BLACKLISTED);
    }
  }
}
