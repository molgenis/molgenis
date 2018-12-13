package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import org.molgenis.navigator.model.Resource;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_GetResourcesResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class GetResourcesResponse {
  /** @return <tt>null</tt> folder implies the root package */
  @CheckForNull
  public abstract Folder getFolder();

  public abstract List<Resource> getResources();

  public static GetResourcesResponse create(Folder newFolder, List<Resource> newResources) {
    return builder().setFolder(newFolder).setResources(newResources).build();
  }

  public static Builder builder() {
    return new AutoValue_GetResourcesResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setFolder(Folder newFolder);

    public abstract Builder setResources(List<Resource> newResources);

    public abstract GetResourcesResponse build();
  }
}
