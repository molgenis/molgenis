package org.molgenis.api.filetransfer.v1;

import static org.molgenis.api.filetransfer.v1.FileTransferApiController.PATH_DOWNLOAD;

import org.molgenis.api.filetransfer.FileDownloadUriGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FileDownloadUriGeneratorImpl implements FileDownloadUriGenerator {
  @Override
  public String generateUri(String fileId) {
    return UriComponentsBuilder.newInstance()
        .pathSegment(FileTransferApiController.URI_API, PATH_DOWNLOAD, fileId)
        .build()
        .toUriString();
  }
}
