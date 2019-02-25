package org.molgenis.api.files.v1;

import org.molgenis.api.files.FileDownloadUriGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FileDownloadUriGeneratorImpl implements FileDownloadUriGenerator {
  @Override
  public String generateUri(String fileId) {
    return UriComponentsBuilder.newInstance()
        .pathSegment(FilesApiV1Namespace.API_PATH, FilesApiV1Namespace.API_DOWNLOAD_PATH, fileId)
        .build()
        .toUriString();
  }
}
