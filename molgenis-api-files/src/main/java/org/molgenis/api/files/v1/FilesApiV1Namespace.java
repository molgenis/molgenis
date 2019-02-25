package org.molgenis.api.files.v1;

import org.molgenis.api.files.FilesApiNamespace;

class FilesApiV1Namespace {
  private FilesApiV1Namespace() {}

  static final int API_VERSION = 1;
  static final String API_PATH = FilesApiNamespace.API_PATH + "/v" + API_VERSION;

  static final String API_DOWNLOAD_PATH = "download";
}
