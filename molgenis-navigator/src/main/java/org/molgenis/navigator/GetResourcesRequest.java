package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class GetResourcesRequest {
  /** <tt>null</tt> folder id implies the root folder */
  @Nullable
  public abstract String getFolderId();

  public static GetResourcesRequest create(String newFolderId) {
    return new AutoValue_GetResourcesRequest(newFolderId);
  }
}
