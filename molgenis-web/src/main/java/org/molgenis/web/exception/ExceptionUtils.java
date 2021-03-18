package org.molgenis.web.exception;

import java.util.Optional;
import org.molgenis.util.exception.ErrorCoded;
import org.springframework.validation.Errors;

class ExceptionUtils {
  private ExceptionUtils() {}

  static Optional<String> getErrorCode(Throwable throwable) {
    Optional<String> errorCode;
    if (throwable instanceof ErrorCoded) {
      errorCode = Optional.of(((ErrorCoded) throwable).getErrorCode());
    } else {
      errorCode = Optional.empty();
    }
    return errorCode;
  }

  /** @return whether the throwable given contains {@link Errors}. */
  static boolean hasErrors(Throwable throwable) {
    return throwable instanceof Errors;
  }

  /** @return {@link Errors} of the given throwable */
  static Optional<Errors> getErrors(Throwable throwable) {
    Optional<Errors> errors;
    if (throwable instanceof Errors) {
      errors = Optional.of((Errors) throwable);
    } else {
      errors = Optional.empty();
    }
    return errors;
  }

  /**
   * @return throwable if it is {@link ErrorCoded}, an error coded cause or an empty throwable if no
   *     error coded cause could be found.
   */
  static Optional<Throwable> getErrorCodedCause(Throwable throwable) {
    for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
      if (cause instanceof ErrorCoded) {
        return Optional.of(cause);
      }
    }
    return Optional.empty();
  }
}
