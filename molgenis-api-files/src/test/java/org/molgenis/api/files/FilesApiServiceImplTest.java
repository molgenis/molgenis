package org.molgenis.api.files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.blob.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FilesApiServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private BlobStore blobStore;
  @Mock private FileUploaderRegistry fileUploadServiceRegistry;
  private FilesApiServiceImpl filesApiServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    filesApiServiceImpl =
        new FilesApiServiceImpl(dataService, blobStore, fileUploadServiceRegistry);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFilesApiServiceImpl() {
    new FilesApiServiceImpl(null, null, null);
  }

  @Test
  public void testGetFileMeta() {
    String fileId = "MyFileId";
    FileMeta fileMeta = mock(FileMeta.class);
    when(dataService.findOneById("sys_FileMeta", fileId, FileMeta.class)).thenReturn(fileMeta);

    assertEquals(filesApiServiceImpl.getFileMeta(fileId), fileMeta);
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testGetFileMetaUnknown() {
    String fileId = "MyFileId";
    filesApiServiceImpl.getFileMeta(fileId);
  }

  @Test
  public void testUpload() throws ExecutionException, InterruptedException {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String contentType = "application/octet-stream";
    when(httpServletRequest.getContentType()).thenReturn(contentType);
    FileUploader fileUploader = mock(FileUploader.class);
    FileMeta fileMeta = mock(FileMeta.class);
    when(fileUploader.upload(httpServletRequest)).thenReturn(fileMeta);
    when(fileUploadServiceRegistry.getFileUploadService(MimeType.valueOf(contentType)))
        .thenReturn(fileUploader);
    assertEquals(filesApiServiceImpl.upload(httpServletRequest).get(), fileMeta);
  }

  @Test
  public void testDownload() {
    String fileId = "MyFileId";
    String contentType = "application/octet-stream";
    String filename = "filename";
    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMeta.getContentType()).thenReturn(contentType);
    when(fileMeta.getFilename()).thenReturn(filename);
    when(dataService.findOneById("sys_FileMeta", fileId, FileMeta.class)).thenReturn(fileMeta);

    ResponseEntity<StreamingResponseBody> responseEntity = filesApiServiceImpl.download(fileId);
    assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseEntity.getHeaders().getContentType(), MediaType.valueOf(contentType));
    assertEquals(
        responseEntity.getHeaders().getContentDisposition(),
        ContentDisposition.parse("attachment; filename=\"filename\""));
  }
}
