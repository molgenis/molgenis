package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeResponse.class)
public abstract class AttributeResponse {

  public abstract LinksResponse getLink();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract Attribute getData();

  public static AttributeResponse create(LinksResponse linksResponse, Attribute newData) {
    return builder().setLink(linksResponse).setData(newData).build();
  }

  public static Builder builder() {
    return new AutoValue_AttributeResponse.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLink(LinksResponse linksResponse);

    public abstract Builder setData(Attribute newData);

    public abstract AttributeResponse build();
  }
}
