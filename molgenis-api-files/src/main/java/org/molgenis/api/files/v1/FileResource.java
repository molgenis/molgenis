package org.molgenis.api.files.v1;

public class FileResource {
  private final String filename;
  private final String contentType;
  private final Long size;

  public FileResource(String filename, String contentType, Long size) {
    this.filename = filename;
    this.contentType = contentType;
    this.size = size;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public Long getSize() {
    return size;
  }
}
