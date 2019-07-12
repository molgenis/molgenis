package org.molgenis.web.exception;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.molgenis.web.exception.Problem.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
class ProblemExceptionResponseGenerator
    implements ExceptionResponseGenerator<ResponseEntity<Problem>> {

  @Override
  public ExceptionResponseType getType() {
    return ExceptionResponseType.PROBLEM;
  }

  @Override
  public ResponseEntity<Problem> createExceptionResponse(
      Exception exception, HttpStatus httpStatus, boolean isDevEnvironment) {

    Builder builder =
        Problem.builder()
            .setType(getProblemType())
            .setStatus(httpStatus.value())
            .setTitle(httpStatus.getReasonPhrase());

    Optional<String> errorCode = ExceptionUtils.getErrorCode(exception);
    if (errorCode.isPresent()) {
      builder.setDetail(exception.getLocalizedMessage());
      builder.setErrorCode(errorCode.get());
    } else if (isDevEnvironment) {
      // only expose exception messages for exceptions without error code in development environment
      builder.setDetail(exception.getLocalizedMessage());
    }

    if (isDevEnvironment) {
      builder.setStackTrace(getStackTrace(exception));
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    return new ResponseEntity<>(builder.build(), httpHeaders, httpStatus);
  }

  private static URI getProblemType() {
    return ServletUriComponentsBuilder.fromCurrentServletMapping()
        .replacePath("problem")
        .replaceQuery(null)
        .fragment(null)
        .build()
        .toUri();
  }

  private static List<String> getStackTrace(Exception exception) {
    return stream(exception.getStackTrace()).map(StackTraceElement::toString).collect(toList());
  }
}
