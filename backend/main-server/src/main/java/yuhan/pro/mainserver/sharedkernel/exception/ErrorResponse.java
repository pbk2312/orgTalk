package yuhan.pro.mainserver.sharedkernel.exception;

public record ErrorResponse(
    String code,
    String message,
    int status,
    String detail
) {

  public static ErrorResponse of(CustomException e) {
    return new ErrorResponse(
        e.getExceptionCode().name(),
        e.getExceptionCode().getMessage(),
        e.getExceptionCode().getHttpStatus().value(),
        e.getDetail()
    );
  }
}
