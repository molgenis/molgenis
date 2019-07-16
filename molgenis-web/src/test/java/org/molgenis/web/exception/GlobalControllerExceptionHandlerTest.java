package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.web.method.HandlerMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GlobalControllerExceptionHandlerTest extends AbstractMockitoTest {
  @Mock private ExceptionHandlerFacade exceptionHandlerFacade;
  private GlobalControllerExceptionHandler globalControllerExceptionHandler;

  @BeforeMethod
  public void setUpBeforeMethod() {
    globalControllerExceptionHandler = new GlobalControllerExceptionHandler(exceptionHandlerFacade);
  }

  @Test
  public void testHandleNotFoundException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleNotFoundException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, NOT_FOUND, method);
  }

  @Test
  public void testHandleConflictException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleConflictException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, CONFLICT, method);
  }

  @Test
  public void testHandleBadRequestException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleBadRequestException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, BAD_REQUEST, method);
  }

  @Test
  public void testHandleForbiddenException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleForbiddenException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, FORBIDDEN, method);
  }

  @Test
  public void testHandleUnauthorizedException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleUnauthorizedException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, UNAUTHORIZED, method);
  }
}
