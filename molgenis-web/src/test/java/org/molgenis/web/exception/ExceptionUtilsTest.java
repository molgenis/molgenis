package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.Optional;
import org.molgenis.i18n.CodedRuntimeException;
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
}
