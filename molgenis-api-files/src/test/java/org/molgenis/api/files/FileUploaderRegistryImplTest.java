package org.molgenis.api.files;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.test.AbstractMockitoTest;
import org.springframework.util.MimeType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FileUploaderRegistryImplTest extends AbstractMockitoTest {
  private FileUploaderRegistryImpl fileUploaderRegistryImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    fileUploaderRegistryImpl = new FileUploaderRegistryImpl();
  }

  @Test
  public void testGetFileUploadService() {
    MimeType mimeType = MimeType.valueOf("application/octet-stream");
    FileUploader fileUploader = mock(FileUploader.class);
    when(fileUploader.getSupportedMimeTypes()).thenReturn(singletonList(mimeType));
    fileUploaderRegistryImpl.register(fileUploader);

    assertEquals(fileUploaderRegistryImpl.getFileUploadService(mimeType), fileUploader);
  }

  @Test(expectedExceptions = UnsupportedMimeTypeException.class)
  public void testGetFileUploadServiceUnknown() {
    MimeType mimeType = MimeType.valueOf("application/json");
    fileUploaderRegistryImpl.getFileUploadService(mimeType);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRegisterAlreadyRegistered() {
    MimeType mimeType = MimeType.valueOf("multipart/form-data");
    FileUploader fileUploader = mock(FileUploader.class);
    when(fileUploader.getSupportedMimeTypes()).thenReturn(singletonList(mimeType));
    FileUploader otherFileUploader = mock(FileUploader.class);
    when(otherFileUploader.getSupportedMimeTypes()).thenReturn(singletonList(mimeType));
    fileUploaderRegistryImpl.register(fileUploader);
    fileUploaderRegistryImpl.register(otherFileUploader);
  }
}
