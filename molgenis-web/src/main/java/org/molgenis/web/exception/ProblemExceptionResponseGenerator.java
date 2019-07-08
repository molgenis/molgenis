package org.molgenis.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.zalando.problem.Status;

@Component
class ProblemExceptionResponseGenerator
    implements ExceptionResponseGenerator<ResponseEntity<Problem>> {

  @Override
  public ExceptionResponseType getType() {
    return ExceptionResponseType.PROBLEM;
  }

  @Override
  public ResponseEntity<Problem> createExceptionResponse(
      Exception exception, HttpStatus httpStatus, boolean isLogStacktrace) {
    String errorCode = ExceptionUtils.getErrorCode(exception).orElse(null);
    Problem problem = new Problem(Status.valueOf(httpStatus.value()), "error message", errorCode);
    if (!isLogStacktrace) {
      problem.setStackTrace(new StackTraceElement[0]);
    }

    return new ResponseEntity<>(problem, httpStatus);
  }
}
