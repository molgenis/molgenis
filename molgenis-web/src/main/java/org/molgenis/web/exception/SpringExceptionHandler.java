package org.molgenis.web.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Based on {@link ResponseEntityExceptionHandler} but allows for returning both {@link
 * ResponseEntity} and {@link ModelAndView}.
 */
public abstract class SpringExceptionHandler {

  protected abstract Object logAndHandleException(
      Exception e, HttpStatus httpStatus, HttpServletRequest request);

  protected abstract Object logAndHandleException(
      Exception e, HttpStatus httpStatus, HandlerMethod handlerMethod);

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public final Object handleSpringMethodNotAllowedException(
      Exception e, HttpServletRequest request) {
    return logAndHandleException(e, METHOD_NOT_ALLOWED, request);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public final Object handleSpringNotFoundException(Exception e, HttpServletRequest request) {
    return logAndHandleException(e, NOT_FOUND, request);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public final Object handleSpringPayloadTooLargeException(
      Exception e, HttpServletRequest request) {
    Exception cause = e;
    while (cause.getCause() instanceof Exception) {
      cause = (Exception) cause.getCause();
    }
    return logAndHandleException(cause, PAYLOAD_TOO_LARGE, request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public final Object handleSpringUnsupportedMediaTypeException(
      Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, UNSUPPORTED_MEDIA_TYPE, handlerMethod);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public final Object handleSpringNotAcceptableException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, NOT_ACCEPTABLE, handlerMethod);
  }

  @ExceptionHandler({
    MissingServletRequestParameterException.class,
    ServletRequestBindingException.class,
    TypeMismatchException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentNotValidException.class,
    MissingServletRequestPartException.class,
    BindException.class,
    ConstraintViolationException.class
  })
  public final Object handleSpringBadRequestException(Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, BAD_REQUEST, handlerMethod);
  }

  @ExceptionHandler(AsyncRequestTimeoutException.class)
  public final Object handleSpringServiceUnavailableException(
      Exception e, HandlerMethod handlerMethod) {
    return logAndHandleException(e, SERVICE_UNAVAILABLE, handlerMethod);
  }
}
