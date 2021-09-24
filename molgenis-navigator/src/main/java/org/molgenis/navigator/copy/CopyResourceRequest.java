package org.molgenis.navigator.copy;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;
import org.molgenis.navigator.model.ResourceIdentifier;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyResourceRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CopyResourceRequest {

  public abstract List<ResourceIdentifier> getResources();

  @Nullable
  @CheckForNull
  public abstract String getTargetPackage();

  public static CopyResourceRequest create(
      List<ResourceIdentifier> resources, String targetPackage) {
    return new AutoValue_CopyResourceRequest(resources, targetPackage);
  }
}
