package org.molgenis.data.file;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BlobMetadata {
  public abstract String getId();

  public abstract long getSize();

  public static BlobMetadata create(String newId, long newSize) {
    return builder().setId(newId).setSize(newSize).build();
  }

  public static Builder builder() {
    return new AutoValue_BlobMetadata.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String newId);

    public abstract Builder setSize(long newSize);

    public abstract BlobMetadata build();
  }
}
