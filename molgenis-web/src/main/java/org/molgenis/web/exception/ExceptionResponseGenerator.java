package org.molgenis.web.exception;

import org.springframework.http.HttpStatus;

interface ExceptionResponseGenerator<T> {

  ExceptionResponseType getType();

  /**
   * @param exception exception for which to create a response
   * @param httpStatus HTTP response status code
   * @param isDevEnvironment whether development environment or not (e.g. to log stack traces)
   * @return exception response (e.g. ResponseEntity or ModelAndView)
   */
  T createExceptionResponse(Exception exception, HttpStatus httpStatus, boolean isDevEnvironment);
}
