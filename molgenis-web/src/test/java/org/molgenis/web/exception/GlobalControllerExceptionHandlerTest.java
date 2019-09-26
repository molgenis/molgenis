package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.web.method.HandlerMethod;

class GlobalControllerExceptionHandlerTest extends AbstractMockitoTest {
  @Mock private ExceptionHandlerFacade exceptionHandlerFacade;
  private GlobalControllerExceptionHandler globalControllerExceptionHandler;

  @BeforeEach
  void setUpBeforeMethod() {
    globalControllerExceptionHandler = new GlobalControllerExceptionHandler(exceptionHandlerFacade);
  }

  @Test
  void testHandleNotFoundException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleNotFoundException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, NOT_FOUND, method);
  }

  @Test
  void testHandleConflictException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleConflictException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, CONFLICT, method);
  }

  @Test
  void testHandleBadRequestException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleBadRequestException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, BAD_REQUEST, method);
  }

  @Test
  void testHandleForbiddenException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleForbiddenException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, FORBIDDEN, method);
  }

  @Test
  void testHandleUnauthorizedException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    globalControllerExceptionHandler.handleUnauthorizedException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, UNAUTHORIZED, method);
  }
}
