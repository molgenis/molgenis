package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Collection.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Collection {
  public abstract String getCollectionId();

  @Nullable
  public abstract String getBiobankId();

  public static Collection create(String collectionId, String biobankId) {
    return new AutoValue_Collection(collectionId, biobankId);
  }
}
