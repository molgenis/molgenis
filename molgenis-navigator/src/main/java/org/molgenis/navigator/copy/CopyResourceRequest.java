package org.molgenis.navigator.copy;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyResourceRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CopyResourceRequest {

  public abstract List<ResourceIdentifier> getResources();

  @CheckForNull
  public abstract String getTargetPackage();

  public static CopyResourceRequest create(
      List<ResourceIdentifier> resources, String targetPackage) {
    return new AutoValue_CopyResourceRequest(resources, targetPackage);
  }
}
