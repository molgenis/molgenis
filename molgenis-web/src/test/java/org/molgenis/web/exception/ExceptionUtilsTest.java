package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import org.molgenis.util.exception.CodedRuntimeException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.testng.annotations.Test;

public class ExceptionUtilsTest {

  @Test
  public void testGetErrorCode() {
    String myErrorCode = "MyErrorCode";
    CodedRuntimeException e = mock(CodedRuntimeException.class);
    when(e.getErrorCode()).thenReturn(myErrorCode);
    assertEquals(ExceptionUtils.getErrorCode(e), Optional.of(myErrorCode));
  }

  @Test
  public void testGetErrorCodeNotExists() {
    Exception e = mock(Exception.class);
    assertEquals(ExceptionUtils.getErrorCode(e), Optional.empty());
  }

  @Test
  public void testHasErrorsTrueBindException() {
    assertTrue(ExceptionUtils.hasErrors(mock(BindException.class)));
  }

  @Test
  public void testHasErrorsTrueMethodArgumentNotValidException() {
    assertTrue(ExceptionUtils.hasErrors(mock(MethodArgumentNotValidException.class)));
  }

  @Test
  public void testHasErrorsFalse() {
    assertFalse(ExceptionUtils.hasErrors(mock(Throwable.class)));
  }

  @Test
  public void testGetErrorsBindException() {
    BindException bindException = mock(BindException.class);
    assertEquals(ExceptionUtils.getErrors(bindException), Optional.of(bindException));
  }

  @Test
  public void testGetErrorsMethodArgumentNotValidException() {
    MethodArgumentNotValidException methodArgumentNotValidException =
        mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    assertEquals(
        ExceptionUtils.getErrors(methodArgumentNotValidException), Optional.of(bindingResult));
  }

  @Test
  public void testGetErrorsEmpty() {
    assertEquals(ExceptionUtils.getErrors(mock(Throwable.class)), Optional.empty());
  }

  @Test
  public void testGetErrorCodedThrowable() {
    Throwable throwable = mock(CodedRuntimeException.class);
    assertEquals(ExceptionUtils.getErrorCodedCause(throwable), Optional.of(throwable));
  }

  @Test
  public void testGetErrorCodedThrowableCause() {
    Throwable throwableCauseCause = mock(CodedRuntimeException.class);
    Throwable throwableCause = mock(Throwable.class);
    Throwable throwable = mock(Throwable.class);
    when(throwable.getCause()).thenReturn(throwableCause);
    when(throwableCause.getCause()).thenReturn(throwableCauseCause);
    assertEquals(ExceptionUtils.getErrorCodedCause(throwable), Optional.of(throwableCauseCause));
  }

  @Test
  public void testGetErrorCodedCauseEmpty() {
    Throwable throwable = mock(Throwable.class);
    assertEquals(ExceptionUtils.getErrorCodedCause(throwable), Optional.empty());
  }
}
