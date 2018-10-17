package org.molgenis.navigator.copy;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.data.resource.Resource;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyResourceRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CopyResourceRequest {

  public abstract List<Resource> getResources();

  @Nullable
  public abstract String getTargetPackage();

  public static CopyResourceRequest create(List<Resource> resources, String targetPackage) {
    return new AutoValue_CopyResourceRequest(resources, targetPackage);
  }
}
