package org.molgenis.api.files.v1;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_FileResponse.class)
public abstract class FileResponse {
  public abstract String getId();

  public abstract String getFilename();

  @Nullable
  public abstract String getContentType();

  @Nullable
  public abstract Long getSize();

  public static Builder builder() {
    return new AutoValue_FileResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String newId);

    public abstract Builder setFilename(String newFilename);

    public abstract Builder setContentType(String newContentType);

    public abstract Builder setSize(Long newSize);

    public abstract FileResponse build();
  }
}
