package org.molgenis.api.files.v1;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.files.FilesApiNamespace.API_FILES_ID;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.data.security.EntityTypePermission.ADD_DATA;
import static org.molgenis.data.security.EntityTypePermission.DELETE_DATA;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.api.ApiController;
import org.molgenis.api.files.FilesApiNamespace;
import org.molgenis.api.files.FilesService;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Api("Files")
@RestController
@RequestMapping(FilesApiNamespace.API_FILES_PATH)
class FilesController extends ApiController {
  private final FilesService filesService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  FilesController(FilesService filesService, UserPermissionEvaluator userPermissionEvaluator) {
    super(API_FILES_ID, 1);
    this.filesService = requireNonNull(filesService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
  }

  @ApiOperation("Upload file (see documentation)")
  @PostMapping
  @ResponseStatus(CREATED)
  public CompletableFuture<ResponseEntity<FileResponse>> createFile(
      HttpServletRequest httpServletRequest) {
    validateCreatePermission();

    return filesService
        .upload(httpServletRequest)
        .thenApply(fileMeta -> this.toFileResponseEntity(fileMeta, httpServletRequest));
  }

  @PostMapping(consumes = {"application/x-www-form-urlencoded", "multipart/form-data"})
  @ResponseStatus(BAD_REQUEST)
  public CompletableFuture<ResponseEntity<FileResponse>> createFileFromForm(
      HttpServletRequest httpServletRequest) {
    throw new UnsupportedOperationException(
        "Media type '" + httpServletRequest.getContentType() + "' not supported");
  }

  @ApiOperation("Retrieve file metadata (see documentation)")
  @GetMapping(value = "/{fileId}")
  public FileResponse readFile(@PathVariable("fileId") String fileId) {
    validateReadPermission();

    FileMeta fileMeta = filesService.getFileMeta(fileId);
    return toFileResponse(fileMeta);
  }

  @ApiOperation("Download file (see documentation)")
  @GetMapping(value = "/{fileId}", params = "alt=media")
  public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId) {
    validateReadPermission();

    return filesService.download(fileId);
  }

  @ApiOperation("Delete file (see documentation)")
  @DeleteMapping(value = "/{fileId}")
  @ResponseStatus(NO_CONTENT)
  public void deleteFile(@PathVariable("fileId") String fileId) {
    validateDeletePermission();

    filesService.delete(fileId);
  }

  private ResponseEntity<FileResponse> toFileResponseEntity(
      FileMeta fileMeta, HttpServletRequest httpServletRequest) {
    FileResponse fileResponse = toFileResponse(fileMeta);

    URI uri =
        ServletUriComponentsBuilder.fromRequestUri(httpServletRequest)
            .pathSegment(fileMeta.getId())
            .queryParam("alt", "media")
            .build()
            .toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uri);

    return new ResponseEntity<>(fileResponse, headers, HttpStatus.CREATED);
  }

  private FileResponse toFileResponse(FileMeta fileMeta) {
    return FileResponse.builder()
        .setId(fileMeta.getId())
        .setFilename(fileMeta.getFilename())
        .setContentType(fileMeta.getContentType())
        .setSize(fileMeta.getSize())
        .build();
  }

  private void validateCreatePermission() {
    validatePermission(ADD_DATA);
  }

  private void validateReadPermission() {
    validatePermission(READ_DATA);
  }

  private void validateDeletePermission() {
    validatePermission(DELETE_DATA);
  }

  private void validatePermission(EntityTypePermission entityTypePermission) {
    boolean hasPermission =
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(FILE_META), entityTypePermission);
    if (!hasPermission) {
      throw new EntityTypePermissionDeniedException(entityTypePermission, FILE_META);
    }
  }
}
