package org.molgenis.api.files.v1;

import java.util.Collection;
import org.molgenis.api.files.v1.model.FilesCreateResponse;
import org.molgenis.data.file.model.FileMeta;

public interface FileMetaMapper {
  FilesCreateResponse toFilesUploadResponse(Collection<FileMeta> fileMetaStream);
}
