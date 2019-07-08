package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.web.method.HandlerMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FallbackExceptionHandlerTest extends AbstractMockitoTest {
  @Mock private ExceptionHandlerFacade exceptionHandlerFacade;
  private FallbackExceptionHandler fallbackExceptionHandler;

  @BeforeMethod
  public void setUpBeforeMethod() {
    fallbackExceptionHandler = new FallbackExceptionHandler(exceptionHandlerFacade);
  }

  @Test
  public void testHandleInternalServerErrorException() {
    Exception e = mock(Exception.class);
    HandlerMethod method = mock(HandlerMethod.class);
    fallbackExceptionHandler.handleInternalServerErrorException(e, method);
    verify(exceptionHandlerFacade).logAndHandleException(e, INTERNAL_SERVER_ERROR, method);
  }
}
