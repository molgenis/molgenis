package org.molgenis.api.filetransfer.v1;

import java.util.Collection;
import org.molgenis.api.filetransfer.v1.model.FilesUploadResponse;
import org.molgenis.data.file.model.FileMeta;

public interface FileMetaMapper {
  FilesUploadResponse toFilesUploadResponse(Collection<FileMeta> fileMetaStream);
}
