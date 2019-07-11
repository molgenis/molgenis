package org.molgenis.web.exception;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExceptionResponseGeneratorRegistrarTest extends AbstractMockitoTest {
  @Mock private ExceptionResponseGeneratorRegistry exceptionResponseGeneratorRegistry;
  private ExceptionResponseGeneratorRegistrar exceptionResponseGeneratorRegistrar;

  @BeforeMethod
  public void setUpBeforeMethod() {
    exceptionResponseGeneratorRegistrar =
        new ExceptionResponseGeneratorRegistrar(exceptionResponseGeneratorRegistry);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testExceptionResponseGeneratorRegistrar() {
    new ExceptionResponseGeneratorRegistrar(null);
  }

  @Test
  public void testRegister() {
    ExceptionResponseGenerator exceptionResponseGenerator = mock(ExceptionResponseGenerator.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getBeansOfType(ExceptionResponseGenerator.class))
        .thenReturn(singletonMap("exceptionResponseGenerator", exceptionResponseGenerator));
    exceptionResponseGeneratorRegistrar.register(applicationContext);
    verify(exceptionResponseGeneratorRegistry)
        .registerExceptionResponseGenerator(exceptionResponseGenerator);
  }
}
