package org.molgenis.api.files;

public class FileUploadException extends RuntimeException {
  FileUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
