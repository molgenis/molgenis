package org.molgenis.api.data.v2;

import com.google.auto.value.AutoValue;
import javax.validation.constraints.NotNull;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyEntityRequestV2.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CopyEntityRequestV2 {
  @NotNull
  public abstract String getNewEntityName();

  public static CopyEntityRequestV2 create(String newEntityName) {
    return new AutoValue_CopyEntityRequestV2(newEntityName);
  }
}
