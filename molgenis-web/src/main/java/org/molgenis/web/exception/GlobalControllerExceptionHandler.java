package org.molgenis.web.exception;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsAnonymous;
import static org.springframework.http.HttpStatus.*;

import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.data.UnknownDataException;
import org.molgenis.data.security.exception.PermissionDeniedException;
import org.molgenis.i18n.BadRequestException;
import org.molgenis.i18n.CodedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalControllerExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

  @Value("${environment:production}")
  private String environment;

  @ExceptionHandler(UnknownDataException.class)
  public Object handleException(UnknownDataException e, HandlerMethod handlerMethod) {
    LOG.info("", e);
    return ExceptionHandlerUtils.handleException(
        e, handlerMethod, NOT_FOUND, e.getErrorCode(), environment);
  }

  @ExceptionHandler(DataAlreadyExistsException.class)
  public Object handleException(DataAlreadyExistsException e, HandlerMethod handlerMethod) {
    LOG.info("", e);
    return ExceptionHandlerUtils.handleException(
        e, handlerMethod, CONFLICT, e.getErrorCode(), environment);
  }

  @ExceptionHandler(BadRequestException.class)
  public Object handleException(BadRequestException e, HandlerMethod handlerMethod) {
    LOG.info("", e);
    return ExceptionHandlerUtils.handleException(
        e, handlerMethod, BAD_REQUEST, e.getErrorCode(), environment);
  }

  @ExceptionHandler(CodedRuntimeException.class)
  public Object handleException(CodedRuntimeException e, HandlerMethod handlerMethod) {
    LOG.info("", e);
    return ExceptionHandlerUtils.handleException(
        e, handlerMethod, NOT_FOUND, e.getErrorCode(), environment);
  }

  @ExceptionHandler
  public Object handlePermissionDeniedException(
      PermissionDeniedException e, HandlerMethod handlerMethod) {
    LOG.info(e.getErrorCode(), e);
    return ExceptionHandlerUtils.handleException(
        e,
        handlerMethod,
        currentUserIsAnonymous() ? UNAUTHORIZED : FORBIDDEN,
        e.getErrorCode(),
        environment);
  }
}
