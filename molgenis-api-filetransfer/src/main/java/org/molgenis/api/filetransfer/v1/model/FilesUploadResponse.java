package org.molgenis.api.filetransfer.v1.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_FilesUploadResponse.class)
public abstract class FilesUploadResponse
{
	public abstract List<FileUploadResponse> getFiles();

	public static FilesUploadResponse create(List<FileUploadResponse> fileUploadResponses)
	{
		return new AutoValue_FilesUploadResponse(fileUploadResponses);
	}
}
