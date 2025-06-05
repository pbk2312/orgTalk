package yuhan.pro.mainserver.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증 관리 API")
public class AuthController {

  private final MemberService memberService;
  private final AuthService authService;

  @Operation(summary = "헤더용 회원 정보 반환")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "회원 정보 반환 성공")
  })
  @GetMapping("/me")
  @ResponseStatus(HttpStatus.OK)
  public MemberResponse getMe() {
    return memberService.isLogin();
  }

  @Operation(summary = "accessToken 재발급")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "accessToken 재발급 성긍"),
      @ApiResponse(responseCode = "401", description = "refreshToken이 유효하지 않음"),
      @ApiResponse(responseCode = "401", description = "refreshToken이 블랙리스트에 존재함")
  })
  @PostMapping("/refresh")
  @ResponseStatus(HttpStatus.OK)
  public AccessTokenResponse getAccessToken(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response
  ) {
    return authService.getAccessToken(refreshToken, response);
  }

  @Operation(summary = "로그아웃")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
  })
  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {
    authService.logout(response, refreshToken);
  }
}
