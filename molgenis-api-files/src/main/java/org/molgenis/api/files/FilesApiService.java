package org.molgenis.api.files;

import java.util.concurrent.CompletableFuture;
import javax.servlet.http.HttpServletRequest;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface FilesApiService {

  /** Asynchronous file upload from HTTP request */
  @Async
  CompletableFuture<FileMeta> upload(HttpServletRequest httpServletRequest);

  /**
   * Asynchronous file download to HTTP response
   *
   * @throws org.molgenis.data.UnknownEntityException if fileId is unknown
   */
  ResponseEntity<StreamingResponseBody> download(String fileId);

  /**
   * Get file metadata
   *
   * @throws org.molgenis.data.UnknownEntityException if fileId is unknown
   */
  FileMeta getFileMeta(String fileId);
}
