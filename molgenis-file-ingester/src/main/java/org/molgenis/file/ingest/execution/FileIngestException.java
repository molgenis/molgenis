package org.molgenis.file.ingest.execution;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class FileIngestException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public FileIngestException() {
    super();
  }

  public FileIngestException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public FileIngestException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileIngestException(String message) {
    super(message);
  }

  public FileIngestException(Throwable cause) {
    super(cause);
  }
}
