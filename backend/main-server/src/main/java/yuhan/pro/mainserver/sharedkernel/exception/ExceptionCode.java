package yuhan.pro.mainserver.sharedkernel.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

  // 400
  GITHUB_EMAIL_NOT_FOUND(BAD_REQUEST, "깃허브 이메일이 존재하지 않습니다."),

  // 404
  MEMBER_NOT_FOUND(NOT_FOUND, "존재하지 않는 회원입니다."),
  ORGANIZATION_NOT_FOUND(NOT_FOUND, "해당되는 조직 ID가 존재하지 않습니다"),

  // 401
  INVALID_JWT_CLAIMS(UNAUTHORIZED, "JWT 클레임이 유효하지 않습니다."),
  REFRESH_TOKEN_INVALID(UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
  REFRESH_TOKEN_BLACKLISTED(UNAUTHORIZED, "차단된 리프레시 토큰입니다.");

  private final HttpStatus httpStatus;
  private final String message;

  ExceptionCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
