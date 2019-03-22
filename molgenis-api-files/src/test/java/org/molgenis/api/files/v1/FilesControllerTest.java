package org.molgenis.api.files.v1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.data.security.EntityTypePermission.ADD_DATA;
import static org.molgenis.data.security.EntityTypePermission.DELETE_DATA;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.molgenis.api.files.FilesService;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FilesControllerTest extends AbstractMockitoTest {
  @Mock private FilesService filesApiService;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  private FilesController filesApiController;

  @BeforeMethod
  public void setUpBeforeMethod() {
    filesApiController = new FilesController(filesApiService, userPermissionEvaluator);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFilesApiController() {
    new FilesController(null, null);
  }

  @Test
  public void testCreateFile() throws ExecutionException, InterruptedException {
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(FILE_META), ADD_DATA))
        .thenReturn(true);

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

  @Test(expectedExceptions = EntityTypePermissionDeniedException.class)
  public void testCreateFileNotPermitted() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    filesApiController.createFile(httpServletRequest);
  }

  @Test(
      expectedExceptions = UnsupportedOperationException.class,
      expectedExceptionsMessageRegExp = "Media type 'multipart/form-data' not supported")
  public void testCreateFileFromForm() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(httpServletRequest.getContentType()).thenReturn("multipart/form-data");
    filesApiController.createFileFromForm(httpServletRequest);
  }

  @Test
  public void testReadFile() {
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(FILE_META), READ_DATA))
        .thenReturn(true);

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

  @Test(expectedExceptions = EntityTypePermissionDeniedException.class)
  public void testReadFileNotPermitted() {
    String fileId = "MyId";
    filesApiController.readFile(fileId);
  }

  @Test
  public void testDownloadFile() {
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(FILE_META), READ_DATA))
        .thenReturn(true);

    String fileId = "MyId";
    @SuppressWarnings("unchecked")
    ResponseEntity<StreamingResponseBody> responseEntity = mock(ResponseEntity.class);
    when(filesApiService.download(fileId)).thenReturn(responseEntity);
    assertEquals(filesApiController.downloadFile(fileId), responseEntity);
  }

  @Test(expectedExceptions = EntityTypePermissionDeniedException.class)
  public void testDownloadFileNotPermitted() {
    String fileId = "MyId";
    filesApiController.downloadFile(fileId);
  }

  @Test
  public void testDeleteFile() {
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(FILE_META), DELETE_DATA))
        .thenReturn(true);

    String fileId = "MyFileId";
    filesApiController.deleteFile(fileId);
    verify(filesApiService).delete(fileId);
  }

  @Test(expectedExceptions = EntityTypePermissionDeniedException.class)
  public void testDeleteFileNotPermitted() {
    String fileId = "MyId";
    filesApiController.deleteFile(fileId);
  }
}
