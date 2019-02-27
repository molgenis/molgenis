package org.molgenis.api.files.v1;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.files.FilesApiNamespace.API_FILES_ID;
import static org.molgenis.api.files.FilesApiNamespace.API_FILES_PATH;
import static org.springframework.http.HttpStatus.CREATED;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.api.ApiController;
import org.molgenis.api.files.FilesService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Api("Files")
@RestController
@RequestMapping(FilesController.API_FILES_V1_PATH)
class FilesController extends ApiController {
  private static final int API_FILES_V1_VERSION = 1;
  static final String API_FILES_V1_PATH = API_FILES_PATH + "/v" + API_FILES_V1_VERSION;

  private final FilesService filesService;

  FilesController(FilesService filesService) {
    super(API_FILES_ID, API_FILES_V1_VERSION);
    this.filesService = requireNonNull(filesService);
  }

  @ApiOperation("Upload file")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "file",
        value = "The file to upload.",
        required = true,
        dataType = "file"),
    @ApiImplicitParam(name = "Content-Type", value = "The file content type.", dataType = "header"),
    @ApiImplicitParam(name = "Content-Length", value = "The file size.", dataType = "header"),
    @ApiImplicitParam(name = "x-molgenis-filename", value = "The filename.", dataType = "header")
  })
  @ApiResponses({
    @ApiResponse(code = 201, message = "Returns the file metadata", response = FileResponse.class)
  })
  @PostMapping
  @ResponseStatus(CREATED)
  public CompletableFuture<ResponseEntity<FileResponse>> createFile(
      HttpServletRequest httpServletRequest) {
    return filesService
        .upload(httpServletRequest)
        .thenApply(fileMeta -> this.toFileResponseEntity(fileMeta, httpServletRequest));
  }

  @ApiOperation("Retrieve file metadata")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Returns the file metadata", response = FileResponse.class),
    @ApiResponse(code = 404, message = "File does not exist")
  })
  @GetMapping(value = "/{fileId}")
  public FileResponse readFile(@PathVariable("fileId") String fileId) {
    FileMeta fileMeta = filesService.getFileMeta(fileId);
    return toFileResponse(fileMeta);
  }

  @ApiOperation("Download one file")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Downloads the file"),
    @ApiResponse(code = 404, message = "File does not exist")
  })
  @GetMapping(value = "/{fileId}", params = "alt=media")
  public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId) {
    return filesService.download(fileId);
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
}
