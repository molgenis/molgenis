package org.molgenis.web.exception;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import org.molgenis.i18n.ContextMessageSource;
import org.molgenis.util.exception.ErrorCoded;
import org.molgenis.web.exception.Problem.Builder;
import org.molgenis.web.exception.Problem.Error;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
class ProblemExceptionResponseGenerator
    implements ExceptionResponseGenerator<ResponseEntity<Problem>> {

  private final ContextMessageSource contextMessageSource;

  ProblemExceptionResponseGenerator(ContextMessageSource contextMessageSource) {
    this.contextMessageSource = requireNonNull(contextMessageSource);
  }

  @Override
  public ExceptionResponseType getType() {
    return ExceptionResponseType.PROBLEM;
  }

  @Override
  public ResponseEntity<Problem> createExceptionResponse(
      Exception exception, HttpStatus httpStatus, boolean isDevEnvironment) {

    Builder builder =
        Problem.builder()
            .setType(getProblemType(exception))
            .setStatus(httpStatus.value())
            .setTitle(httpStatus.getReasonPhrase());

    ExceptionUtils.getErrorCode(exception)
        .ifPresent(
            errorCode ->
                builder.setErrorCode(errorCode).setDetail(exception.getLocalizedMessage()));

    ExceptionUtils.getErrors(exception)
        .ifPresent(errors -> builder.setErrors(createProblemErrors(errors)));

    if (isDevEnvironment) {
      // only expose exception messages for exceptions without error code in dev environment
      builder.setDetail(exception.getLocalizedMessage()).setStackTrace(getStackTrace(exception));
    }

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    return new ResponseEntity<>(builder.build(), httpHeaders, httpStatus);
  }

  private List<Problem.Error> createProblemErrors(Errors errors) {
    Stream<Error> fieldErrors = errors.getFieldErrors().stream().map(this::createProblemError);
    Stream<Error> globalErrors = errors.getGlobalErrors().stream().map(this::createProblemError);
    return Stream.concat(fieldErrors, globalErrors).collect(toList());
  }

  private Problem.Error createProblemError(FieldError fieldError) {
    Error.Builder builder = Error.builder();

    buildProblemError(fieldError, builder);
    builder.setField(fieldError.getField());

    Object rejectedValue = fieldError.getRejectedValue();
    if (rejectedValue != null) {
      builder.setValue(rejectedValue.toString());
    }

    buildProblemError(fieldError, builder);

    return builder.build();
  }

  private Problem.Error createProblemError(ObjectError objectError) {
    Error.Builder builder = Error.builder();
    buildProblemError(objectError, builder);
    return builder.build();
  }

  private void buildProblemError(ObjectError objectError, Error.Builder builder) {
    if (objectError.contains(ConstraintViolation.class)) {
      buildProblemErrorConstraintViolation(objectError, builder);
    } else if (objectError.contains(Throwable.class)) {
      buildProblemErrorThrowable(objectError, builder);
    } else {
      builder.setDetail("An error occurred.");
    }
  }

  private void buildProblemErrorThrowable(ObjectError objectError, Error.Builder builder) {
    Throwable unwrappedThrowable = objectError.unwrap(Throwable.class);
    ExceptionUtils.getErrorCodedCause(unwrappedThrowable)
        .ifPresentOrElse(
            throwable -> {
              builder.setErrorCode(((ErrorCoded) throwable).getErrorCode());
              builder.setDetail(throwable.getLocalizedMessage());
            },
            () ->
                builder.setDetail(
                    contextMessageSource.getMessage(
                        "org.molgenis.web.exception.ObjectError.generic")));
  }

  private void buildProblemErrorConstraintViolation(
      ObjectError objectError, Error.Builder builder) {
    String detail = null;
    String detailCode = null;

    String[] codes = objectError.getCodes();
    if (codes != null) {
      for (String code : codes) {
        String message = contextMessageSource.getMessage(code, objectError.getArguments());
        if (message != null && !message.equals('#' + code + '#')) {
          detail = message;
          detailCode = code;
          break;
        }
      }
    }

    builder.setDetail(detail != null ? detail : objectError.getDefaultMessage());
    builder.setErrorCode(detailCode != null ? detailCode : objectError.getCode());
  }

  private static URI getProblemType(Throwable throwable) {
    String path = ExceptionUtils.hasErrors(throwable) ? "input-invalid" : "problem";
    return ServletUriComponentsBuilder.fromCurrentServletMapping()
        .replacePath(path)
        .replaceQuery(null)
        .fragment(null)
        .build()
        .toUri();
  }

  private static List<String> getStackTrace(Exception exception) {
    return stream(exception.getStackTrace()).map(StackTraceElement::toString).collect(toList());
  }
}
