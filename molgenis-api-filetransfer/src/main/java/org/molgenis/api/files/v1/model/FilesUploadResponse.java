package org.molgenis.api.filetransfer.v1.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_FilesUploadResponse.class)
public abstract class FilesUploadResponse {
  public abstract List<FileUploadResponse> getFiles();

  public static FilesUploadResponse create(List<FileUploadResponse> fileUploadResponses) {
    return new AutoValue_FilesUploadResponse(fileUploadResponses);
  }
}
