package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PackageResponse.class)
public abstract class PackageResponse {
  public abstract LinksResponse getLinks();

  public static PackageResponse create(
      LinksResponse newLinks) {
    return builder().setLinks(newLinks).build();
  }

  public static Builder builder() {
    return new AutoValue_PackageResponse.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract PackageResponse build();
  }
}
