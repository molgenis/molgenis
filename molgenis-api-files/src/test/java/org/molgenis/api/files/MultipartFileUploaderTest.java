package org.molgenis.api.files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.primitives.Bytes;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.blob.BlobMetadata;
import org.molgenis.data.blob.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.util.MimeType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MultipartFileUploaderTest extends AbstractMockitoTest {
  @Mock private BlobStore blobStore;
  @Mock private FileMetaFactory fileMetaFactory;
  @Mock private DataService dataService;
  private MultipartFileUploader multipartFileUploader;

  @BeforeMethod
  public void setUpBeforeMethod() {
    multipartFileUploader = new MultipartFileUploader(blobStore, fileMetaFactory, dataService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testMultipartFileUploader() {
    new MultipartFileUploader(null, null, null);
  }

  @Test
  public void testGetSupportedMimeTypes() {
    assertEquals(
        multipartFileUploader.getSupportedMimeTypes(),
        Arrays.asList(
            MimeType.valueOf("multipart/form-data"), MimeType.valueOf("multipart/mixed")));
  }

  @Test
  public void testUpload() {
    String filename = "test.zip";
    String contentType = "application/zip";
    HttpServletRequest httpServletRequest = createMockHttpServletRequest(filename, contentType);

    String blobId = "MyBlobId";
    Long size = 3L;
    BlobMetadata blobMetadata = mock(BlobMetadata.class);
    when(blobMetadata.getId()).thenReturn(blobId);
    when(blobMetadata.getSize()).thenReturn(size);
    when(blobStore.store(any())).thenReturn(blobMetadata);

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(blobId)).thenReturn(fileMeta);

    assertEquals(multipartFileUploader.upload(httpServletRequest), fileMeta);
    verify(fileMeta).setFilename(filename);
    verify(fileMeta).setContentType(contentType);
    verify(fileMeta).setSize(size);
  }

  private HttpServletRequest createMockHttpServletRequest(String filename, String contentType) {
    MockMultipartHttpServletRequest httpServletRequest = new MockMultipartHttpServletRequest();
    byte[] data = new byte[] {0x00, 0x01, 0x02};
    MockMultipartFile file = new MockMultipartFile(filename, filename, contentType, data);
    String boundary = "q1w2e3r4t5y6u7i8o9";
    httpServletRequest.setContentType("multipart/form-data; boundary=" + boundary);
    httpServletRequest.setContent(createFileContent(data, boundary, contentType, filename));
    httpServletRequest.addFile(file);
    httpServletRequest.setMethod("POST");
    return httpServletRequest;
  }

  private byte[] createFileContent(
      byte[] data, String boundary, String contentType, String fileName) {
    String start =
        "--"
            + boundary
            + "\r\n Content-Disposition: form-data; name=\"file\"; filename=\""
            + fileName
            + "\"\r\n"
            + "Content-type: "
            + contentType
            + "\r\n\r\n";
    String end = "\r\n--" + boundary + "--";
    return Bytes.concat(start.getBytes(UTF_8), data, end.getBytes(UTF_8));
  }
}
