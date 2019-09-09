package org.molgenis.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

class FileUploadUtilsTest {
  private Part part;

  @BeforeEach
  void setUp() {
    part = mock(Part.class);
  }

  @Test
  void getOriginalFileName() {
    when(part.getHeader("content-disposition"))
        .thenReturn("form-data; name=\"upload\"; filename=\"example.xls\"");
    String filename = FileUploadUtils.getOriginalFileName(part);
    assertEquals(filename, "example.xls");
  }

  @Test
  void getOriginalFileNameIE() {
    // In internet explorer the filename part of the disposition contains the path
    when(part.getHeader("content-disposition"))
        .thenReturn(
            "form-data; name=\"upload\"; filename=\"c:"
                + File.separator
                + "test"
                + File.separator
                + "path"
                + File.separator
                + "example.xls\"");
    String filename = FileUploadUtils.getOriginalFileName(part);
    assertEquals(filename, "example.xls");
  }

  @Test
  void getOriginalFileNameWithMissingHeader() {
    when(part.getHeader("content-disposition")).thenReturn(null);
    assertNull(FileUploadUtils.getOriginalFileName(part));
  }

  @Test
  void getOriginalFileNameNoFileSelected() {
    when(part.getHeader("content-disposition"))
        .thenReturn("form-data; name=\"upload\"; filename=\"\"");
    String filename = FileUploadUtils.getOriginalFileName(part);
    assertNull(filename);
  }

  @Test
  void saveToTempFile() throws UnsupportedEncodingException, IOException {
    String fileContent = "Hey dude";
    when(part.getHeader("content-disposition"))
        .thenReturn("form-data; name=\"upload\"; filename=\"example.txt\"");
    when(part.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent.getBytes("UTF-8")));

    File tempFile = FileUploadUtils.saveToTempFile(part);

    try {
      assertNotNull(tempFile);
      assertTrue(tempFile.exists());
      assertEquals(FileCopyUtils.copyToString(new FileReader(tempFile)), fileContent);
    } finally {
      tempFile.delete();
    }
  }
}
