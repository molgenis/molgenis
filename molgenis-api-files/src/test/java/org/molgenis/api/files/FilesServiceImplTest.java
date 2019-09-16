package org.molgenis.api.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.ContentDisposition.parse;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.valueOf;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.BlobMetadata;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

class FilesServiceImplTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private BlobStore blobStore;
  @Mock private FileMetaFactory fileMetaFactory;
  private FilesServiceImpl filesApiServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    filesApiServiceImpl = new FilesServiceImpl(dataService, blobStore, fileMetaFactory);
  }

  @Test
  void testFilesApiServiceImpl() {
    assertThrows(NullPointerException.class, () -> new FilesServiceImpl(null, null, null));
  }

  @Test
  void testGetFileMeta() {
    String fileId = "MyFileId";
    FileMeta fileMeta = mock(FileMeta.class);
    when(dataService.findOneById("sys_FileMeta", fileId, FileMeta.class)).thenReturn(fileMeta);

    assertEquals(fileMeta, filesApiServiceImpl.getFileMeta(fileId));
  }

  @Test
  void testDeleteFile() {
    String fileId = "MyFileId";
    filesApiServiceImpl.delete(fileId);
    verify(dataService).deleteById("sys_FileMeta", fileId);
  }

  @Test
  void testGetFileMetaUnknown() {
    String fileId = "MyFileId";
    assertThrows(UnknownEntityException.class, () -> filesApiServiceImpl.getFileMeta(fileId));
  }

  @Test
  void testUpload() throws ExecutionException, InterruptedException {
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

    assertEquals(fileMeta, filesApiServiceImpl.upload(httpServletRequest).get());
    verify(fileMeta).setContentType(contentType);
    verify(fileMeta).setSize(1L);
    verify(fileMeta).setFilename(filename);
    verify(fileMeta).setUrl("/MyBlobId?alt=media");
    verifyNoMoreInteractions(fileMeta);
  }

  @Test
  void testDownload() {
    String fileId = "MyFileId";
    String contentType = "application/octet-stream";
    String filename = "filename";
    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMeta.getContentType()).thenReturn(contentType);
    when(fileMeta.getFilename()).thenReturn(filename);
    when(dataService.findOneById("sys_FileMeta", fileId, FileMeta.class)).thenReturn(fileMeta);

    ResponseEntity<StreamingResponseBody> responseEntity = filesApiServiceImpl.download(fileId);
    assertEquals(OK, responseEntity.getStatusCode());
    assertEquals(valueOf(contentType), responseEntity.getHeaders().getContentType());
    assertEquals(
        parse("attachment; filename=\"filename\""),
        responseEntity.getHeaders().getContentDisposition());
  }
}
