package org.molgenis.api.files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileUploaderRegistrarTest extends AbstractMockitoTest {
  @Mock private FileUploaderRegistry fileUploaderRegistry;
  private FileUploaderRegistrar fileUploaderRegistrar;

  @BeforeMethod
  public void setUpBeforeMethod() {
    fileUploaderRegistrar = new FileUploaderRegistrar(fileUploaderRegistry);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFileUploaderRegistrar() {
    new FileUploaderRegistrar(null);
  }

  @Test
  public void testRegister() {
    ContextRefreshedEvent contextRefreshedEvent = mock(ContextRefreshedEvent.class);
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    FileUploader fileUploader = mock(FileUploader.class);
    Map<String, FileUploader> fileUploaderMap = Collections.singletonMap("key", fileUploader);
    when(applicationContext.getBeansOfType(FileUploader.class)).thenReturn(fileUploaderMap);
    when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
    fileUploaderRegistrar.register(contextRefreshedEvent);
    verify(fileUploaderRegistry).register(fileUploader);
  }
}
