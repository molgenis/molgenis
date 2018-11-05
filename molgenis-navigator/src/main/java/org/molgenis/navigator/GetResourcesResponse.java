package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class GetResourcesResponse {
  /**
   * @return <tt>null</tt> folder implies the root package
   */
  @Nullable
  public abstract Folder getFolder();

  public abstract List<Resource> getResources();

  public static GetResourcesResponse create(Folder newFolder, List<Resource> newResources) {
    return new AutoValue_GetResourcesResponse(newFolder, newResources);
  }
}
