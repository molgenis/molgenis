package org.molgenis.api.files.v1;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.files.v1.FilesApiV1Namespace.API_PATH;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.molgenis.api.ApiController;
import org.molgenis.api.files.FileDownloadService;
import org.molgenis.api.files.FileUploadService;
import org.molgenis.api.files.FilesApiNamespace;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.hateoas.Resource;
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
@RequestMapping(API_PATH)
class FilesApiController extends ApiController {
  private final FileUploadService fileUploadService;
  private final FileDownloadService fileDownloadService;

  FilesApiController(FileUploadService fileUploadService, FileDownloadService fileDownloadService) {
    super(FilesApiNamespace.API_ID, FilesApiV1Namespace.API_VERSION);
    this.fileUploadService = requireNonNull(fileUploadService);
    this.fileDownloadService = requireNonNull(fileDownloadService);
  }

  @PostMapping(headers = "Content-Type=multipart/form-data")
  @ResponseStatus(CREATED)
  public CompletableFuture<ResponseEntity<FileResource>> createFile(HttpServletRequest request)
      throws IOException, FileUploadException {
    ServletFileUpload servletFileUpload = new ServletFileUpload();
    FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);
    return fileUploadService.upload(fileItemIterator).thenApply(this::toFileResourceResponse);
  }

  @GetMapping(value = "/{fileId}", produces = "application/hal+json")
  public Resource<FileResource> readFile(@PathVariable("fileId") String fileId) {
    FileMeta fileMeta = fileUploadService.getFileMeta(fileId);
    return toFileResource(fileMeta);
  }

  /** Non-blocking file download. */
  @GetMapping(value = "/{fileId}", params = "alt=media")
  public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId) {
    return fileDownloadService.download(fileId);
  }

  private ResponseEntity<FileResource> toFileResourceResponse(List<FileMeta> fileMetas) {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(linkTo(FilesApiController.class).slash(fileMetas.get(0).getId()).toUri());
    return new ResponseEntity<>(headers, HttpStatus.CREATED);
  }

  private Resource<FileResource> toFileResource(FileMeta fileMeta) {
    FileResource fileResource =
        new FileResource(fileMeta.getFilename(), fileMeta.getContentType(), fileMeta.getSize());
    return new Resource<>(
        fileResource, linkTo(FilesApiController.class).slash(fileMeta.getId()).withSelfRel());
  }
}
