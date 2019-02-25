package org.molgenis.api.filetransfer.v1;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.api.filetransfer.v1.FileTransferApiV1Namespace.API_PATH;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.molgenis.api.ApiController;
import org.molgenis.api.filetransfer.FileDownloadService;
import org.molgenis.api.filetransfer.FileTransferApiNamespace;
import org.molgenis.api.filetransfer.FileUploadService;
import org.molgenis.api.filetransfer.v1.model.FileUploadResponse;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Api("File transfer")
@RestController
@RequestMapping(API_PATH)
class FileTransferApiController extends ApiController {
  private final FileUploadService fileUploadService;
  private final FileDownloadService fileDownloadService;

  FileTransferApiController(
      FileUploadService fileUploadService, FileDownloadService fileDownloadService) {
    super(FileTransferApiNamespace.API_ID, FileTransferApiV1Namespace.API_VERSION);
    this.fileUploadService = requireNonNull(fileUploadService);
    this.fileDownloadService = requireNonNull(fileDownloadService);
  }

  /** Non-blocking multi-file upload. */
  @ApiOperation(value = "Uploads files", consumes = "multipart/form-data")
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "file",
        value = "File to upload",
        required = true,
        dataType = "java.io.File",
        paramType = "form")
  })
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "Update succeeded",
        response = FileUploadResponse.class,
        responseContainer =
            "List") // TODO discuss: do not return list but object so it can be extended
  })
  @PostMapping(value = "/upload", headers = "Content-Type=multipart/form-data")
  public @ResponseBody CompletableFuture<List<FileUploadResponse>> uploadFiles(
      HttpServletRequest request) throws IOException, FileUploadException {
    ServletFileUpload servletFileUpload = new ServletFileUpload();
    FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);
    return fileUploadService.upload(fileItemIterator).thenApply(this::toFileUploadResponses);
  }

  private List<FileUploadResponse> toFileUploadResponses(List<FileMeta> fileMetas) {
    return fileMetas.stream().map(this::toFileUploadResponse).collect(toList());
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
  @GetMapping(value = '/' + FileTransferApiV1Namespace.API_DOWNLOAD_PATH + "/{fileId}")
  public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("fileId") String fileId) {
    return fileDownloadService.download(fileId);
  }
}
