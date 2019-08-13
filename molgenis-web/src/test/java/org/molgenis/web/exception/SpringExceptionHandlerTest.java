package org.molgenis.web.exception;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import javax.servlet.http.HttpServletRequest;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SpringExceptionHandlerTest extends AbstractMockitoTest {
  private SpringExceptionHandler springExceptionHandler;

  @BeforeMethod
  public void setUpBeforeMethod() {
    springExceptionHandler = mock(SpringExceptionHandler.class, CALLS_REAL_METHODS);
  }

  @Test
  public void testHandleSpringMethodNotAllowedException() {
    Exception e = mock(Exception.class);
    HttpServletRequest request = new MockHttpServletRequest();
    springExceptionHandler.handleSpringMethodNotAllowedException(e, request);
    verify(springExceptionHandler).logAndHandleException(e, METHOD_NOT_ALLOWED, request);
  }

  @Test
  public void testHandleSpringNotFoundException() {
    Exception e = mock(Exception.class);
    HttpServletRequest request = new MockHttpServletRequest();
    springExceptionHandler.handleSpringNotFoundException(e, request);
    verify(springExceptionHandler).logAndHandleException(e, NOT_FOUND, request);
  }

  @Test
  public void testHandleSpringPayloadTooLargeException() {
    Exception e = mock(Exception.class);
    HttpServletRequest request = new MockHttpServletRequest();
    Exception cause = mock(Exception.class);
    when(e.getCause()).thenReturn(cause);
    springExceptionHandler.handleSpringPayloadTooLargeException(e, request);
    verify(springExceptionHandler).logAndHandleException(cause, PAYLOAD_TOO_LARGE, request);
  }

  @Test
  public void testHandleSpringUnsupportedMediaTypeException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    springExceptionHandler.handleSpringUnsupportedMediaTypeException(e, method);
    verify(springExceptionHandler).logAndHandleException(e, UNSUPPORTED_MEDIA_TYPE, method);
  }

  @Test
  public void testHandleSpringNotAcceptableException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    springExceptionHandler.handleSpringNotAcceptableException(e, method);
    verify(springExceptionHandler).logAndHandleException(e, NOT_ACCEPTABLE, method);
  }

  @Test
  public void testHandleSpringBadRequestException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    springExceptionHandler.handleSpringBadRequestException(e, method);
    verify(springExceptionHandler).logAndHandleException(e, BAD_REQUEST, method);
  }

  @Test
  public void testAsyncRequestTimeoutException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    springExceptionHandler.handleSpringServiceUnavailableException(e, method);
    verify(springExceptionHandler).logAndHandleException(e, SERVICE_UNAVAILABLE, method);
  }
}
