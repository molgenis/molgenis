package org.molgenis.api.filetransfer.v1;

import org.molgenis.api.filetransfer.FileDownloadUriGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FileDownloadUriGeneratorImpl implements FileDownloadUriGenerator {
  @Override
  public String generateUri(String fileId) {
    return UriComponentsBuilder.newInstance()
        .pathSegment(
            FileTransferApiV1Namespace.API_PATH,
            FileTransferApiV1Namespace.API_DOWNLOAD_PATH,
            fileId)
        .build()
        .toUriString();
  }
}
