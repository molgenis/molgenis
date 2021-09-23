package org.molgenis.web.exception;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.exception.ExceptionUtils.getErrorCode;
import static org.molgenis.web.exception.ExceptionUtils.getErrorCodedCause;
import static org.molgenis.web.exception.ExceptionUtils.getErrors;

import org.junit.jupiter.api.Test;
import org.molgenis.util.exception.CodedRuntimeException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ExceptionUtilsTest {

  @Test
  void testGetErrorCode() {
    String myErrorCode = "MyErrorCode";
    CodedRuntimeException e = mock(CodedRuntimeException.class);
    when(e.getErrorCode()).thenReturn(myErrorCode);
    assertEquals(of(myErrorCode), getErrorCode(e));
  }

  @Test
  void testGetErrorCodeNotExists() {
    Exception e = mock(Exception.class);
    assertEquals(empty(), getErrorCode(e));
  }

  @Test
  void testHasErrorsTrueBindException() {
    assertTrue(ExceptionUtils.hasErrors(mock(BindException.class)));
  }

  @Test
  void testHasErrorsTrueMethodArgumentNotValidException() {
    assertTrue(ExceptionUtils.hasErrors(mock(MethodArgumentNotValidException.class)));
  }

  @Test
  void testHasErrorsFalse() {
    assertFalse(ExceptionUtils.hasErrors(mock(Throwable.class)));
  }

  @Test
  void testGetErrorsBindException() {
    BindException bindException = mock(BindException.class);
    assertEquals(of(bindException), getErrors(bindException));
  }

  @Test
  void testGetErrorsEmpty() {
    assertEquals(empty(), getErrors(mock(Throwable.class)));
  }

  @Test
  void testGetErrorCodedThrowable() {
    Throwable throwable = mock(CodedRuntimeException.class);
    assertEquals(of(throwable), getErrorCodedCause(throwable));
  }

  @Test
  void testGetErrorCodedThrowableCause() {
    Throwable throwableCauseCause = mock(CodedRuntimeException.class);
    Throwable throwableCause = mock(Throwable.class);
    Throwable throwable = mock(Throwable.class);
    when(throwable.getCause()).thenReturn(throwableCause);
    when(throwableCause.getCause()).thenReturn(throwableCauseCause);
    assertEquals(of(throwableCauseCause), getErrorCodedCause(throwable));
  }

  @Test
  void testGetErrorCodedCauseEmpty() {
    Throwable throwable = mock(Throwable.class);
    assertEquals(empty(), getErrorCodedCause(throwable));
  }
}
