package org.molgenis.api.files;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface FileDownloadService {
  ResponseEntity<StreamingResponseBody> download(String fileId);
}
