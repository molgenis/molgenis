package org.molgenis.api.files;

import static org.testng.Assert.assertEquals;

import org.molgenis.test.AbstractMockitoTest;
import org.springframework.util.MimeType;
import org.testng.annotations.Test;

public class UnsupportedMimeTypeExceptionTest extends AbstractMockitoTest {
  @Test
  public void testGetMessage() {
    MimeType mimeType = MimeType.valueOf("application/octet-stream");
    assertEquals(
        new UnsupportedMimeTypeException(mimeType).getMessage(),
        "Unsupported MIME type 'application/octet-stream'");
  }
}
