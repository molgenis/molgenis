package org.molgenis.web.exception;

import java.util.Optional;
import org.molgenis.util.exception.ErrorCoded;

class ExceptionUtils {
  private ExceptionUtils() {}

  static Optional<String> getErrorCode(Exception e) {
    String errorCode;
    if (e instanceof ErrorCoded) {
      errorCode = ((ErrorCoded) e).getErrorCode();
    } else {
      errorCode = null;
    }
    return Optional.ofNullable(errorCode);
  }
}
