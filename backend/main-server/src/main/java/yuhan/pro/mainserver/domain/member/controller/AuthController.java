package yuhan.pro.mainserver.domain.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.mainserver.domain.member.dto.MemberResponse;
import yuhan.pro.mainserver.domain.member.service.AuthService;
import yuhan.pro.mainserver.domain.member.service.MemberService;
import yuhan.pro.mainserver.sharedkernel.jwt.dto.AccessTokenResponse;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final MemberService memberService;
  private final AuthService authService;

  @GetMapping("/me")
  @ResponseStatus(HttpStatus.OK)
  public MemberResponse getMe() {
    log.info("Get member info");
    return memberService.isLogin();
  }

  @PostMapping("/refresh")
  @ResponseStatus(HttpStatus.OK)
  public AccessTokenResponse getAccessToken(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response
  ) {
    return authService.getAccessToken(refreshToken, response);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {
    authService.logout(response, refreshToken);
  }
}
