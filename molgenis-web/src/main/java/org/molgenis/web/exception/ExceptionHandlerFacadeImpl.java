package org.molgenis.web.exception;

import static java.util.Objects.requireNonNull;
import static org.molgenis.web.exception.ExceptionResponseType.ERROR_MESSAGES;
import static org.molgenis.web.exception.ExceptionResponseType.MODEL_AND_VIEW;
import static org.molgenis.web.exception.ExceptionResponseType.PROBLEM;

import javax.servlet.http.HttpServletRequest;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

@Component
class ExceptionHandlerFacadeImpl implements ExceptionHandlerFacade {
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerFacadeImpl.class);

  private final RequestMatcher asyncRequestMatcher;
  private final ExceptionResponseGeneratorRegistry responseGeneratorRegistry;

  @Value("${environment:@null}")
  private String environment;

  ExceptionHandlerFacadeImpl(ExceptionResponseGeneratorRegistry responseGeneratorRegistry) {
    this.responseGeneratorRegistry = requireNonNull(responseGeneratorRegistry);
    this.asyncRequestMatcher =
        new ELRequestMatcher("hasHeader('X-Requested-With','XMLHttpRequest')");
  }

  @Override
  public Object logAndHandleException(
      Exception exception, HttpStatus httpStatus, HandlerMethod handlerMethod) {
    ExceptionResponseType responseType = getExceptionResponseType(handlerMethod);
    return logAndHandleException(exception, httpStatus, responseType);
  }

  @Override
  public Object logAndHandleException(
      Exception exception, HttpStatus httpStatus, HttpServletRequest request) {
    ExceptionResponseType responseType = getExceptionResponseType(request);
    return logAndHandleException(exception, httpStatus, responseType);
  }

  private Object logAndHandleException(
      Exception exception, HttpStatus httpStatus, ExceptionResponseType responseType) {
    logException(exception, httpStatus);

    ExceptionResponseGenerator<?> responseGenerator =
        responseGeneratorRegistry.getExceptionResponseGenerator(responseType);
    return responseGenerator.createExceptionResponse(exception, httpStatus, isLogStackTraces());
  }

  private void logException(Exception exception, HttpStatus httpStatus) {
    if (httpStatus.is4xxClientError()) {
      LOG.warn("", exception);
    } else {
      LOG.error("", exception);
    }
  }

  private boolean isLogStackTraces() {
    return "development".equals(environment);
  }

  private ExceptionResponseType getExceptionResponseType(HandlerMethod handlerMethod) {
    ExceptionResponseType exceptionResponseType;

    Class<?> beanType = handlerMethod.getBeanType();
    if (ApiController.class.isAssignableFrom(beanType)) {
      exceptionResponseType = PROBLEM;
    } else if (handlerMethod.hasMethodAnnotation(ResponseStatus.class)
        || handlerMethod.hasMethodAnnotation(ResponseBody.class)
        || beanType.isAnnotationPresent(ResponseBody.class)
        || beanType.isAnnotationPresent(RestController.class)) {
      exceptionResponseType = ERROR_MESSAGES;
    } else {
      exceptionResponseType = MODEL_AND_VIEW;
    }

    return exceptionResponseType;
  }

  private ExceptionResponseType getExceptionResponseType(HttpServletRequest request) {
    ExceptionResponseType exceptionResponseType;

    String path = request.getRequestURI().substring(request.getContextPath().length());
    if (path.equals(ApiNamespace.API_PATH) || path.startsWith(ApiNamespace.API_PATH + '/')) {
      exceptionResponseType = PROBLEM;
    } else if (HttpMethod.OPTIONS.name().equals(request.getMethod())
        || asyncRequestMatcher.matches(request)) {
      exceptionResponseType = ERROR_MESSAGES;
    } else {
      exceptionResponseType = MODEL_AND_VIEW;
    }

    return exceptionResponseType;
  }
}
