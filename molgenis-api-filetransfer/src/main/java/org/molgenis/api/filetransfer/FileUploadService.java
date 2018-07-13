package org.molgenis.api.filetransfer;

import org.apache.commons.fileupload.FileItemIterator;
import org.molgenis.data.file.model.FileMeta;

import java.util.List;

public interface FileUploadService
{
	List<FileMeta> upload(FileItemIterator fileItemIterator);
}
