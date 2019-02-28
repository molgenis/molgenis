package org.molgenis.api.files;

import static java.util.Objects.requireNonNull;

import org.springframework.util.MimeType;

class UnsupportedMimeTypeException extends RuntimeException {
  private final MimeType mimeType;

  UnsupportedMimeTypeException(MimeType mimeType) {
    this.mimeType = requireNonNull(mimeType);
  }

  @Override
  public String getMessage() {
    return String.format("Unsupported MIME type '%s'", mimeType.toString());
  }
}
