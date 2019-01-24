package org.molgenis.api.filetransfer;

import java.util.List;
import org.apache.commons.fileupload.FileItemIterator;
import org.molgenis.data.file.model.FileMeta;

public interface FileUploadService {
  List<FileMeta> upload(FileItemIterator fileItemIterator);
}
