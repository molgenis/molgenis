package org.molgenis.api.files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.BlobMetadata;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FilesServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private BlobStore blobStore;
  @Mock private FileMetaFactory fileMetaFactory;
  private FilesServiceImpl filesApiServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    filesApiServiceImpl = new FilesServiceImpl(dataService, blobStore, fileMetaFactory);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFilesApiServiceImpl() {
    new FilesServiceImpl(null, null, null);
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
    String blobId = "MyBlobId";
    BlobMetadata blobMetadata = when(mock(BlobMetadata.class).getId()).thenReturn(blobId).getMock();
    when(blobMetadata.getSize()).thenReturn(1L);
    when(blobStore.store(any())).thenReturn(blobMetadata);

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(blobId)).thenReturn(fileMeta);

    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    httpServletRequest.setContent(new byte[] {0x00});
    String contentType = "application/octet-stream";
    httpServletRequest.setContentType(contentType);
    String filename = "myfile.bin";
    httpServletRequest.addHeader("x-molgenis-filename", filename);

    assertEquals(filesApiServiceImpl.upload(httpServletRequest).get(), fileMeta);
    verify(fileMeta).setContentType(contentType);
    verify(fileMeta).setSize(1L);
    verify(fileMeta).setFilename(filename);
    verify(fileMeta).setUrl("/MyBlobId?alt=media");
    verifyNoMoreInteractions(fileMeta);
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
