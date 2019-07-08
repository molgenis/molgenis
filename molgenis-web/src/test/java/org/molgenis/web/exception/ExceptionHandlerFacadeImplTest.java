package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.web.exception.ExceptionResponseType.ERROR_MESSAGES;
import static org.molgenis.web.exception.ExceptionResponseType.MODEL_AND_VIEW;
import static org.molgenis.web.exception.ExceptionResponseType.PROBLEM;

import org.mockito.Mock;
import org.molgenis.api.ApiController;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExceptionHandlerFacadeImplTest extends AbstractMockitoTest {
  @Mock private ExceptionResponseGeneratorRegistry exceptionResponseGeneratorRegistry;
  private ExceptionHandlerFacadeImpl exceptionHandlerFacadeImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    exceptionHandlerFacadeImpl = new ExceptionHandlerFacadeImpl(exceptionResponseGeneratorRegistry);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLogAndHandleExceptionHandlerMethodProblem() {
    Exception e = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBeanType()).thenReturn((Class) ApiController.class);

    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(exceptionResponseGeneratorRegistry.getExceptionResponseGenerator(PROBLEM))
        .thenReturn(responseGenerator);

    exceptionHandlerFacadeImpl.logAndHandleException(e, httpStatus, handlerMethod);
    verify(responseGenerator).createExceptionResponse(e, httpStatus, false);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLogAndHandleExceptionHandlerMethodErrorMessages() {
    Exception e = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBeanType()).thenReturn((Class) String.class);
    when(handlerMethod.hasMethodAnnotation(ResponseStatus.class)).thenReturn(true);

    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(exceptionResponseGeneratorRegistry.getExceptionResponseGenerator(ERROR_MESSAGES))
        .thenReturn(responseGenerator);

    exceptionHandlerFacadeImpl.logAndHandleException(e, httpStatus, handlerMethod);
    verify(responseGenerator).createExceptionResponse(e, httpStatus, false);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testLogAndHandleExceptionHandlerMethodModelAndView() {
    Exception e = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    HandlerMethod handlerMethod = mock(HandlerMethod.class);
    when(handlerMethod.getBeanType()).thenReturn((Class) String.class);

    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(exceptionResponseGeneratorRegistry.getExceptionResponseGenerator(MODEL_AND_VIEW))
        .thenReturn(responseGenerator);

    exceptionHandlerFacadeImpl.logAndHandleException(e, httpStatus, handlerMethod);
    verify(responseGenerator).createExceptionResponse(e, httpStatus, false);
  }

  @Test
  public void testLogAndHandleExceptionHttpServletRequestProblem() {
    Exception e = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("/context");
    request.setRequestURI("/context/api");
    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(exceptionResponseGeneratorRegistry.getExceptionResponseGenerator(PROBLEM))
        .thenReturn(responseGenerator);

    exceptionHandlerFacadeImpl.logAndHandleException(e, httpStatus, request);
    verify(responseGenerator).createExceptionResponse(e, httpStatus, false);
  }

  @Test
  public void testLogAndHandleExceptionHttpServletRequestErrorMessages() {
    Exception e = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod(HttpMethod.OPTIONS.name());

    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(exceptionResponseGeneratorRegistry.getExceptionResponseGenerator(ERROR_MESSAGES))
        .thenReturn(responseGenerator);

    exceptionHandlerFacadeImpl.logAndHandleException(e, httpStatus, request);
    verify(responseGenerator).createExceptionResponse(e, httpStatus, false);
  }

  @Test
  public void testLogAndHandleExceptionHttpServletRequestModelAndView() {
    Exception e = mock(Exception.class);
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    MockHttpServletRequest request = new MockHttpServletRequest();

    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(exceptionResponseGeneratorRegistry.getExceptionResponseGenerator(MODEL_AND_VIEW))
        .thenReturn(responseGenerator);

    exceptionHandlerFacadeImpl.logAndHandleException(e, httpStatus, request);
    verify(responseGenerator).createExceptionResponse(e, httpStatus, false);
  }
}
