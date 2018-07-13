package org.molgenis.api.filetransfer.v1;

import org.molgenis.api.filetransfer.v1.model.FileUploadResponse;
import org.molgenis.api.filetransfer.v1.model.FilesUploadResponse;
import org.molgenis.data.file.model.FileMeta;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class FileMetaMapperImpl implements FileMetaMapper
{
	@Override
	public FilesUploadResponse toFilesUploadResponse(Collection<FileMeta> fileMetaStream)
	{
		List<FileUploadResponse> fileUploadResponses = fileMetaStream.stream()
																	 .map(this::toFileUploadResponse)
																	 .collect(toList());
		return FilesUploadResponse.create(fileUploadResponses);
	}

	private FileUploadResponse toFileUploadResponse(FileMeta fileMeta)
	{
		return FileUploadResponse.builder()
								 .setId(fileMeta.getId())
								 .setFilename(fileMeta.getFilename())
								 .setContentType(fileMeta.getContentType())
								 .setSize(fileMeta.getSize())
								 .setUrl(fileMeta.getUrl())
								 .build();
	}
}
