package yuhan.pro.chatserver.sharedkernel.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

  // 400
  GITHUB_EMAIL_NOT_FOUND(BAD_REQUEST, "깃허브 이메일이 존재하지 않습니다."),
  INVALID_LANGUAGE_VALUE(BAD_REQUEST, "지원하지 않는 언어입니다."),
  PRIVATE_ROOM_PASSWORD_IS_EMPTY(BAD_REQUEST, "비밀번호가 비어있습니다."),
  PRIVATE_ROOM_PASSWORD_NOT_MATCH(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

  // 401
  INVALID_JWT_CLAIMS(UNAUTHORIZED, "JWT 클레임이 유효하지 않습니다."),
  REFRESH_TOKEN_INVALID(UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
  REFRESH_TOKEN_BLACKLISTED(UNAUTHORIZED, "차단된 리프레시 토큰입니다."),
  AUTHENTICATION_NOT_FOUND(UNAUTHORIZED, "인증 정보가 없습니다."),
  MEMBER_NOT_ACCEPTED(UNAUTHORIZED, "해당 채팅방에 접근이 불가합니다."),

  // 403
  ROOM_MEMBER_NOT_FOUND(FORBIDDEN, "채팅방 멤버가 아닙니다."),
  ROOM_OWNER_MISMATCH(FORBIDDEN, "채팅방 방장이 아닙니다."),

  // 404,
  MEMBER_NOT_FOUND(NOT_FOUND, "존재하지 않는 회원입니다."),
  ORGANIZATION_NOT_FOUND(NOT_FOUND, "해당되는 조직 ID가 존재하지 않습니다"),
  ROOM_ID_NOT_FOUND(NOT_FOUND, "해당하는 채팅방이 없습니다."),

  // 409
  CHAT_ROOM_NAME_DUPLICATE(CONFLICT, "이미 존재하는 채팅방 이름입니다."),

  ;


  private final HttpStatus httpStatus;
  private final String message;

  ExceptionCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
