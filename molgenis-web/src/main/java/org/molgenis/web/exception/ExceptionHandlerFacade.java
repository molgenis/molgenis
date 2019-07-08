package org.molgenis.web.exception;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

public interface ExceptionHandlerFacade {
  /** Logs exception and creates a response. */
  Object logAndHandleException(
      Exception exception, HttpStatus httpStatus, HandlerMethod handlerMethod);

  /** Logs exception and creates a response. Use in case of no handler method. */
  Object logAndHandleException(
      Exception exception, HttpStatus httpStatus, HttpServletRequest request);
}
