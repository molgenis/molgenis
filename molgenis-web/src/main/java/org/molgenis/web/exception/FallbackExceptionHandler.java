package org.molgenis.web.exception;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;

@ControllerAdvice
@Order
public class FallbackExceptionHandler {
  private final ExceptionHandlerFacade exceptionHandlerFacade;

  FallbackExceptionHandler(ExceptionHandlerFacade exceptionHandlerFacade) {
    this.exceptionHandlerFacade = requireNonNull(exceptionHandlerFacade);
  }

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler
  public Object handleInternalServerErrorException(Exception e, HandlerMethod handlerMethod) {
    return exceptionHandlerFacade.logAndHandleException(e, INTERNAL_SERVER_ERROR, handlerMethod);
  }
}
