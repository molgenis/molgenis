package org.molgenis.api.data.v2;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_TagResponseV2.class)
public abstract class TagResponseV2 {
  public abstract String relationIRI();

  @Nullable
  @CheckForNull
  public abstract String relationLabel();

  public abstract String objectIRI();

  @Nullable
  @CheckForNull
  public abstract String objectLabel();

  public static TagResponseV2 create(
      String relationIRI, String relationLabel, String objectIRI, String objectLabel) {
    return new AutoValue_TagResponseV2(relationIRI, relationLabel, objectIRI, objectLabel);
  }
}
