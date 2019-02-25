package org.molgenis.api.filetransfer.v1;

import org.molgenis.api.filetransfer.FileTransferApiNamespace;

class FileTransferApiV1Namespace {
  private FileTransferApiV1Namespace() {}

  static final int API_VERSION = 1;
  static final String API_PATH = FileTransferApiNamespace.API_PATH + "/v" + API_VERSION;

  static final String API_DOWNLOAD_PATH = "download";
}
