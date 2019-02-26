package org.molgenis.api.files;

import org.springframework.util.MimeType;

/** Selects a {@link FileUploader} based on a given HTTP request */
interface FileUploaderRegistry {

  /**
   * @throws UnsupportedMimeTypeException if no file uploader can be found for the given MIME type.
   */
  FileUploader getFileUploadService(MimeType mimeType);

  /**
   * @throws IllegalArgumentException if a file uploader already was registered for a MIME type.
   */
  void register(FileUploader fileUploadService);
}
