package org.molgenis.api.files;

public interface FileDownloadUriGenerator {
  String generateUri(String fileId);
}
