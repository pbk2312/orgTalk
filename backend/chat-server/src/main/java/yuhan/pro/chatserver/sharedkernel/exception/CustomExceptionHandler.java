package yuhan.pro.chatserver.sharedkernel.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(
      HttpServletRequest request,
      final CustomException e
  ) {
    ErrorResponse error = ErrorResponse.of(e);

    log.error(
        "Exception @ {} {} → code: {}, message: {}, detail: {}",
        request.getMethod(),
        request.getRequestURI(),
        error.code(),
        error.message(),
        error.detail()
    );

    return ResponseEntity
        .status(e.getExceptionCode().getHttpStatus())
        .body(error);
  }
}
