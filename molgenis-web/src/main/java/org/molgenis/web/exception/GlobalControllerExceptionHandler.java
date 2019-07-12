package org.molgenis.web.exception;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.data.UnknownDataException;
import org.molgenis.data.security.exception.PermissionDeniedException;
import org.molgenis.i18n.BadRequestException;
import org.molgenis.i18n.ForbiddenException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalControllerExceptionHandler extends SpringExceptionHandler {
  private final ExceptionHandlerFacade exceptionHandlerFacade;

  GlobalControllerExceptionHandler(ExceptionHandlerFacade exceptionHandlerFacade) {
    this.exceptionHandlerFacade = requireNonNull(exceptionHandlerFacade);
  }

  @ExceptionHandler(UnknownDataException.class)
  public Object handleNotFoundException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, NOT_FOUND, handlerMethod);
  }

  @ExceptionHandler(DataAlreadyExistsException.class)
  public Object handleConflictException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, CONFLICT, handlerMethod);
  }

  @ExceptionHandler(BadRequestException.class)
  public Object handleBadRequestException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, BAD_REQUEST, handlerMethod);
  }

  @ExceptionHandler(ForbiddenException.class)
  public Object handleForbiddenException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, FORBIDDEN, handlerMethod);
  }

  @ExceptionHandler(PermissionDeniedException.class)
  public Object handleUnauthorizedException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, UNAUTHORIZED, handlerMethod);
  }

  @Override
  protected Object logAndHandleException(
      Exception e, HttpStatus httpStatus, HttpServletRequest request) {
    return exceptionHandlerFacade.logAndHandleException(e, httpStatus, request);
  }

  @Override
  protected Object logAndHandleException(
      Exception e, HttpStatus httpStatus, HandlerMethod handlerMethod) {
    return exceptionHandlerFacade.logAndHandleException(e, httpStatus, handlerMethod);
  }
}
