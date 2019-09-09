package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.web.method.HandlerMethod;

class FallbackExceptionHandlerTest extends AbstractMockitoTest {
  @Mock private ExceptionHandlerFacade exceptionHandlerFacade;
  private FallbackExceptionHandler fallbackExceptionHandler;

  @BeforeEach
  void setUpBeforeMethod() {
    fallbackExceptionHandler = new FallbackExceptionHandler(exceptionHandlerFacade);
  }

  @Test
  void testHandleInternalServerErrorException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    fallbackExceptionHandler.handleInternalServerErrorException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, INTERNAL_SERVER_ERROR, method);
  }
}
