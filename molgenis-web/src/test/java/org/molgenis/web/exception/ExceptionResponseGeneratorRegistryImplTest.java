package org.molgenis.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.exception.ExceptionResponseType.PROBLEM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.util.UnexpectedEnumException;

class ExceptionResponseGeneratorRegistryImplTest {
  private ExceptionResponseGeneratorRegistryImpl exceptionResponseGeneratorRegistryImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    exceptionResponseGeneratorRegistryImpl = new ExceptionResponseGeneratorRegistryImpl();
  }

  @Test
  void testRegisterExceptionResponseGenerator() {
    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(responseGenerator.getType()).thenReturn(PROBLEM);
    exceptionResponseGeneratorRegistryImpl.registerExceptionResponseGenerator(responseGenerator);
    assertEquals(
        responseGenerator,
        exceptionResponseGeneratorRegistryImpl.getExceptionResponseGenerator(PROBLEM));
  }

  @Test
  void testRegisterExceptionResponseGeneratorAlreadyExists() {
    ExceptionResponseGenerator responseGenerator = mock(ExceptionResponseGenerator.class);
    when(responseGenerator.getType()).thenReturn(PROBLEM);
    exceptionResponseGeneratorRegistryImpl.registerExceptionResponseGenerator(responseGenerator);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            exceptionResponseGeneratorRegistryImpl.registerExceptionResponseGenerator(
                responseGenerator));
  }

  @Test
  void testGetExceptionResponseGenerator() {
    assertThrows(
        UnexpectedEnumException.class,
        () -> exceptionResponseGeneratorRegistryImpl.getExceptionResponseGenerator(PROBLEM));
  }
}
