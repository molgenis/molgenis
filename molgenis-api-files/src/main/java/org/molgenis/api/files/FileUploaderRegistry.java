package org.molgenis.api.files;

import org.springframework.util.MimeType;

/** Selects a {@link FileUploader} based on a given HTTP request */
interface FileUploaderRegistry {
  FileUploader getFileUploadService(MimeType mimeType);

  void register(FileUploader fileUploadService);
}
