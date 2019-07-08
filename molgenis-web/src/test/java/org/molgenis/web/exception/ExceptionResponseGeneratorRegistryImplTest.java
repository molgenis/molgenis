package org.molgenis.web.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.exception.ExceptionResponseType.PROBLEM;
import static org.testng.Assert.assertEquals;

import org.molgenis.util.UnexpectedEnumException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExceptionResponseGeneratorRegistryImplTest {
  private ExceptionResponseGeneratorRegistryImpl exceptionResponseGeneratorRegistryImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    exceptionResponseGeneratorRegistryImpl = new ExceptionResponseGeneratorRegistryImpl();
  }

  @Test
  public void testRegisterExceptionResponseGenerator() {
    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(responseGenerator.getType()).thenReturn(PROBLEM);
    exceptionResponseGeneratorRegistryImpl.registerExceptionResponseGenerator(responseGenerator);
    assertEquals(
        exceptionResponseGeneratorRegistryImpl.getExceptionResponseGenerator(PROBLEM),
        responseGenerator);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRegisterExceptionResponseGeneratorAlreadyExists() {
    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(responseGenerator.getType()).thenReturn(PROBLEM);
    exceptionResponseGeneratorRegistryImpl.registerExceptionResponseGenerator(responseGenerator);
    exceptionResponseGeneratorRegistryImpl.registerExceptionResponseGenerator(responseGenerator);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testGetExceptionResponseGenerator() {
    exceptionResponseGeneratorRegistryImpl.getExceptionResponseGenerator(PROBLEM);
  }
}
