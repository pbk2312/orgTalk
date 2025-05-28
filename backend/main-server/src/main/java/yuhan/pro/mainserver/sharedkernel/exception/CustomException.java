package yuhan.pro.mainserver.sharedkernel.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ExceptionCode exceptionCode;
  private final String detail;

  public CustomException(ExceptionCode exceptionCode, String detail) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
    this.detail = detail;
  }

  public CustomException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
    this.detail = "";
  }

  public CustomException(ExceptionCode exceptionCode, Object... args) {
    super(String.format(exceptionCode.getMessage(), args));
    this.exceptionCode = exceptionCode;
    this.detail = "";
  }
}
