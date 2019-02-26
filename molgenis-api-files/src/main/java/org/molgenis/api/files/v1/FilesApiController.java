package org.molgenis.api.files.v1;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.files.FilesApiNamespace.API_FILES_ID;
import static org.molgenis.api.files.FilesApiNamespace.API_FILES_PATH;
import static org.molgenis.api.files.v1.FilesApiController.API_FILES_V1_PATH;
import static org.springframework.http.HttpStatus.CREATED;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.api.ApiController;
import org.molgenis.api.files.FileService;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping(API_FILES_V1_PATH)
class FilesApiController extends ApiController {
  private static final int API_FILES_V1_VERSION = 1;
  static final String API_FILES_V1_PATH = API_FILES_PATH + "/v" + API_FILES_V1_VERSION;

  private final FileService fileService;

  FilesApiController(FileService fileService) {
    super(API_FILES_ID, API_FILES_V1_VERSION);
    this.fileService = requireNonNull(fileService);
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public CompletableFuture<ResponseEntity<FileResponse>> createFile(
      HttpServletRequest httpServletRequest) {
    return fileService.upload(httpServletRequest).thenApply(this::toFileResponseEntity);
  }

  @GetMapping(value = "/{fileId}")
  public FileResponse readFile(@PathVariable("fileId") String fileId) {
    FileMeta fileMeta = fileService.getFileMeta(fileId);
    return toFileResponse(fileMeta);
  }

  /** Non-blocking file download. */
  @GetMapping(value = "/{fileId}", params = "alt=media")
  public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId) {
    return fileService.download(fileId);
  }

  private ResponseEntity<FileResponse> toFileResponseEntity(FileMeta fileMeta) {
    FileResponse fileResponse = toFileResponse(fileMeta);

    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(fileMeta.getUrl()));

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
}
