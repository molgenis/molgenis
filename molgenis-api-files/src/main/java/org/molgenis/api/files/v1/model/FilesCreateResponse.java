package org.molgenis.api.files.v1.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_FilesCreateResponse.class)
public abstract class FilesCreateResponse {
  public abstract List<FileCreateResponse> getFiles();

  public static FilesCreateResponse create(List<FileCreateResponse> fileUploadResponses) {
    return new AutoValue_FilesCreateResponse(fileUploadResponses);
  }
}
