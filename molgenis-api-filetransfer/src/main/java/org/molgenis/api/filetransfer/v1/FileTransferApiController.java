package org.molgenis.api.filetransfer.v1;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.filetransfer.v1.FileTransferApiController.URI_API;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.molgenis.api.filetransfer.FileDownloadService;
import org.molgenis.api.filetransfer.FileTransferApiNamespace;
import org.molgenis.api.filetransfer.FileUploadService;
import org.molgenis.api.filetransfer.v1.model.FileUploadResponse;
import org.molgenis.api.filetransfer.v1.model.FilesUploadResponse;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping(URI_API)
class FileTransferApiController {
  private static final String VERSION = "v1";
  static final String URI_API = FileTransferApiNamespace.URI_API + '/' + VERSION;
  static final String PATH_DOWNLOAD = "download";

  private final FileUploadService fileUploadService;
  private final FileDownloadService fileDownloadService;

  FileTransferApiController(
      FileUploadService fileUploadService, FileDownloadService fileDownloadService) {
    this.fileUploadService = requireNonNull(fileUploadService);
    this.fileDownloadService = requireNonNull(fileDownloadService);
  }

  /** Non-blocking multi-file upload. */
  @PostMapping(value = "/upload", headers = "Content-Type=multipart/form-data")
  public @ResponseBody DeferredResult<FilesUploadResponse> uploadFiles(HttpServletRequest request)
      throws IOException, FileUploadException {
    ServletFileUpload servletFileUpload = new ServletFileUpload();
    FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);

    DeferredResult<FilesUploadResponse> deferredResult = new DeferredResult<>();
    deferredResult.onError(
        (Throwable t) -> {
          deferredResult.setErrorResult(
              ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body("An error occurred: " + t.getMessage()));
        });
    deferredResult.onTimeout(
        () -> {
          deferredResult.setErrorResult(
              ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timeout occurred."));
        });

    ForkJoinPool.commonPool()
        .submit(
            () -> {
              List<FileMeta> fileMetaList = fileUploadService.upload(fileItemIterator);
              FilesUploadResponse filesUploadResponse =
                  FilesUploadResponse.create(
                      fileMetaList.stream().map(this::toFileUploadResponse).collect(toList()));
              deferredResult.setResult(filesUploadResponse);
            });

    return deferredResult;
  }

  private FileUploadResponse toFileUploadResponse(FileMeta fileMeta) {
    return FileUploadResponse.builder()
        .setId(fileMeta.getId())
        .setFilename(fileMeta.getFilename())
        .setContentType(fileMeta.getContentType())
        .setSize(fileMeta.getSize())
        .setUrl(fileMeta.getUrl())
        .build();
  }

  /** Non-blocking file download. */
  @GetMapping(value = '/' + PATH_DOWNLOAD + "/{fileId}")
  public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId) {
    return fileDownloadService.download(fileId);
  }
}
