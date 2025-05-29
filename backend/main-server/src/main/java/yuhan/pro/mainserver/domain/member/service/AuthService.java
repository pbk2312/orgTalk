package yuhan.pro.mainserver.domain.member.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yuhan.pro.mainserver.sharedkernel.jwt.JwtValidator;
import yuhan.pro.mainserver.sharedkernel.jwt.TokenProvider;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.AccessTokenResponse;
import yuhan.pro.mainserver.sharedkernel.util.CookieUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final TokenProvider tokenProvider;
  private final JwtValidator jwtValidator;

  public AccessTokenResponse getAccessToken(String refreshToken,
      HttpServletResponse response) {
    validateRefreshToken(refreshToken, response);

    return tokenProvider.getAccessToken(refreshToken);
  }

  private void validateRefreshToken(String refreshToken, HttpServletResponse response) {
    if (!jwtValidator.validate(refreshToken)) {
      log.warn("Invalid refresh token: {}", refreshToken);
      CookieUtils.removeCookie(response, "refreshToken");
    }
  }
}
