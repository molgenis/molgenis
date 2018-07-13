package org.molgenis.api.filetransfer.v1;

import org.molgenis.api.filetransfer.v1.model.FilesUploadResponse;
import org.molgenis.data.file.model.FileMeta;

import java.util.Collection;

public interface FileMetaMapper
{
	FilesUploadResponse toFilesUploadResponse(Collection<FileMeta> fileMetaStream);
}
