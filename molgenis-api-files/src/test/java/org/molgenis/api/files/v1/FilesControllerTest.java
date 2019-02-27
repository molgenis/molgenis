package org.molgenis.api.files.v1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.molgenis.api.files.FilesService;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FilesControllerTest extends AbstractMockitoTest {
  @Mock private FilesService filesApiService;
  private FilesController filesApiController;

  @BeforeMethod
  public void setUpBeforeMethod() {
    filesApiController = new FilesController(filesApiService);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFilesApiController() {
    new FilesController(null);
  }

  @Test
  public void testCreateFile() throws ExecutionException, InterruptedException {
    FileMeta fileMeta = mock(FileMeta.class);
    String fileId = "MyId";
    String filename = "MyFilename";
    String contentType = "MyContentType";
    Long size = 123L;
    when(fileMeta.getId()).thenReturn(fileId);
    when(fileMeta.getFilename()).thenReturn(filename);
    when(fileMeta.getContentType()).thenReturn(contentType);
    when(fileMeta.getSize()).thenReturn(size);

    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(filesApiService.upload(httpServletRequest))
        .thenReturn(CompletableFuture.completedFuture(fileMeta));
    ResponseEntity<FileResponse> fileResponseResponseEntity =
        filesApiController.createFile(httpServletRequest).get();

    FileResponse expectedFileResponse =
        FileResponse.builder()
            .setId(fileId)
            .setFilename(filename)
            .setContentType(contentType)
            .setSize(size)
            .build();
    assertEquals(fileResponseResponseEntity.getBody(), expectedFileResponse);
    assertEquals(fileResponseResponseEntity.getStatusCode(), HttpStatus.CREATED);
    assertNotNull(fileResponseResponseEntity.getHeaders().getLocation());
  }

  @Test
  public void testReadFile() {
    FileMeta fileMeta = mock(FileMeta.class);
    String fileId = "MyId";
    String filename = "MyFilename";
    String contentType = "MyContentType";
    Long size = 123L;
    when(fileMeta.getId()).thenReturn(fileId);
    when(fileMeta.getFilename()).thenReturn(filename);
    when(fileMeta.getContentType()).thenReturn(contentType);
    when(fileMeta.getSize()).thenReturn(size);

    when(filesApiService.getFileMeta(fileId)).thenReturn(fileMeta);
    FileResponse fileResponse = filesApiController.readFile(fileId);
    FileResponse expectedFileResponse =
        FileResponse.builder()
            .setId(fileId)
            .setFilename(filename)
            .setContentType(contentType)
            .setSize(size)
            .build();
    assertEquals(fileResponse, expectedFileResponse);
  }

  @Test
  public void testDownloadFile() {
    String fileId = "MyId";
    @SuppressWarnings("unchecked")
    ResponseEntity<StreamingResponseBody> responseEntity = mock(ResponseEntity.class);
    when(filesApiService.download(fileId)).thenReturn(responseEntity);
    assertEquals(filesApiController.downloadFile(fileId), responseEntity);
  }
}
