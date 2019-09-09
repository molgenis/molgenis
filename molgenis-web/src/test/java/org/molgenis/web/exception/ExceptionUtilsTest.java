package org.molgenis.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.molgenis.util.exception.CodedRuntimeException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ExceptionUtilsTest {

  @Test
  void testGetErrorCode() {
    String myErrorCode = "MyErrorCode";
    CodedRuntimeException e = mock(CodedRuntimeException.class);
    when(e.getErrorCode()).thenReturn(myErrorCode);
    assertEquals(ExceptionUtils.getErrorCode(e), Optional.of(myErrorCode));
  }

  @Test
  void testGetErrorCodeNotExists() {
    Exception e = mock(Exception.class);
    assertEquals(ExceptionUtils.getErrorCode(e), Optional.empty());
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
    assertEquals(ExceptionUtils.getErrors(bindException), Optional.of(bindException));
  }

  @Test
  void testGetErrorsMethodArgumentNotValidException() {
    MethodArgumentNotValidException methodArgumentNotValidException =
        mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    assertEquals(
        ExceptionUtils.getErrors(methodArgumentNotValidException), Optional.of(bindingResult));
  }

  @Test
  void testGetErrorsEmpty() {
    assertEquals(ExceptionUtils.getErrors(mock(Throwable.class)), Optional.empty());
  }

  @Test
  void testGetErrorCodedThrowable() {
    Throwable throwable = mock(CodedRuntimeException.class);
    assertEquals(ExceptionUtils.getErrorCodedCause(throwable), Optional.of(throwable));
  }

  @Test
  void testGetErrorCodedThrowableCause() {
    Throwable throwableCauseCause = mock(CodedRuntimeException.class);
    Throwable throwableCause = mock(Throwable.class);
    Throwable throwable = mock(Throwable.class);
    when(throwable.getCause()).thenReturn(throwableCause);
    when(throwableCause.getCause()).thenReturn(throwableCauseCause);
    assertEquals(ExceptionUtils.getErrorCodedCause(throwable), Optional.of(throwableCauseCause));
  }

  @Test
  void testGetErrorCodedCauseEmpty() {
    Throwable throwable = mock(Throwable.class);
    assertEquals(ExceptionUtils.getErrorCodedCause(throwable), Optional.empty());
  }
}
