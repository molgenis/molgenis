package org.molgenis.api.files.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.data.security.EntityTypePermission.ADD_DATA;
import static org.molgenis.data.security.EntityTypePermission.DELETE_DATA;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.files.FilesService;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

class FilesControllerTest extends AbstractMockitoTest {
  @Mock private FilesService filesApiService;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  private FilesController filesApiController;

  @BeforeEach
  void setUpBeforeMethod() {
    filesApiController = new FilesController(filesApiService, userPermissionEvaluator);
  }

  @Test
  void testFilesApiController() {
    assertThrows(NullPointerException.class, () -> new FilesController(null, null));
  }

  @Test
  void testCreateFile() throws ExecutionException, InterruptedException {
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
    assertEquals(expectedFileResponse, fileResponseResponseEntity.getBody());
    assertEquals(CREATED, fileResponseResponseEntity.getStatusCode());
    assertNotNull(fileResponseResponseEntity.getHeaders().getLocation());
  }

  @Test
  void testCreateFileNotPermitted() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    assertThrows(
        EntityTypePermissionDeniedException.class,
        () -> filesApiController.createFile(httpServletRequest));
  }

  @Test
  void testCreateFileFromForm() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(httpServletRequest.getContentType()).thenReturn("multipart/form-data");
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> filesApiController.createFileFromForm(httpServletRequest));
    assertThat(exception.getMessage())
        .containsPattern("Media type 'multipart/form-data' not supported");
  }

  @Test
  void testReadFile() {
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
    assertEquals(expectedFileResponse, fileResponse);
  }

  @Test
  void testReadFileNotPermitted() {
    String fileId = "MyId";
    assertThrows(
        EntityTypePermissionDeniedException.class, () -> filesApiController.readFile(fileId));
  }

  @Test
  void testDownloadFile() {
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(FILE_META), READ_DATA))
        .thenReturn(true);

    String fileId = "MyId";
    @SuppressWarnings("unchecked")
    ResponseEntity<StreamingResponseBody> responseEntity = mock(ResponseEntity.class);
    when(filesApiService.download(fileId)).thenReturn(responseEntity);
    assertEquals(responseEntity, filesApiController.downloadFile(fileId));
  }

  @Test
  void testDownloadFileNotPermitted() {
    String fileId = "MyId";
    assertThrows(
        EntityTypePermissionDeniedException.class, () -> filesApiController.downloadFile(fileId));
  }

  @Test
  void testDeleteFile() {
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(FILE_META), DELETE_DATA))
        .thenReturn(true);

    String fileId = "MyFileId";
    filesApiController.deleteFile(fileId);
    verify(filesApiService).delete(fileId);
  }

  @Test
  void testDeleteFileNotPermitted() {
    String fileId = "MyId";
    assertThrows(
        EntityTypePermissionDeniedException.class, () -> filesApiController.deleteFile(fileId));
  }
}
