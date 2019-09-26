package org.molgenis.web.exception;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;

class ExceptionResponseGeneratorRegistrarTest extends AbstractMockitoTest {
  @Mock private ExceptionResponseGeneratorRegistry exceptionResponseGeneratorRegistry;
  private ExceptionResponseGeneratorRegistrar exceptionResponseGeneratorRegistrar;

  @BeforeEach
  void setUpBeforeMethod() {
    exceptionResponseGeneratorRegistrar =
        new ExceptionResponseGeneratorRegistrar(exceptionResponseGeneratorRegistry);
  }

  @Test
  void testExceptionResponseGeneratorRegistrar() {
    assertThrows(NullPointerException.class, () -> new ExceptionResponseGeneratorRegistrar(null));
  }

  @Test
  void testRegister() {
    ExceptionResponseGenerator exceptionResponseGenerator = mock(ExceptionResponseGenerator.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getBeansOfType(ExceptionResponseGenerator.class))
        .thenReturn(singletonMap("exceptionResponseGenerator", exceptionResponseGenerator));
    exceptionResponseGeneratorRegistrar.register(applicationContext);
    verify(exceptionResponseGeneratorRegistry)
        .registerExceptionResponseGenerator(exceptionResponseGenerator);
  }
}
