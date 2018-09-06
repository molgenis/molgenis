package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MetaDataRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class MetaDataRequest {

  public abstract @Nullable Collection<String> getAttributes();

  public abstract @Nullable Collection<String> getExpands();

  public static MetaDataRequest create() {
    return new AutoValue_MetaDataRequest(null, null);
  }

  public static MetaDataRequest create(Collection<String> attributes, Collection<String> expands) {
    return new AutoValue_MetaDataRequest(attributes, expands);
  }
}
