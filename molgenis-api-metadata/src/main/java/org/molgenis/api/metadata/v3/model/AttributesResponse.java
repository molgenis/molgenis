package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData.Builder;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributesResponse.class)
public abstract class AttributesResponse {
  public abstract LinksResponse getLinks();

  @Nullable
  @CheckForNull
  public abstract String getLabelAttribute();

  @Nullable
  @CheckForNull
  public abstract String getIdAttribute();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract List<AttributeResponse> getItems();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract PageResponse getPage();

  public static AttributesResponse create(LinksResponse newLinks) {
    return create(newLinks, null, null);
  }

  public static AttributesResponse create(
      LinksResponse newLinks, List<AttributeResponse> attributeResponses, PageResponse newPage) {
    return builder().setLinks(newLinks).setItems(attributeResponses).setPage(newPage).build();
  }

  public static Builder builder() {
    return new AutoValue_AttributesResponse.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setLabelAttribute(String attributeId);

    public abstract Builder setIdAttribute(String attributeId);

    public abstract Builder setItems(List<AttributeResponse> newItems);

    public abstract Builder setPage(PageResponse newPage);

    public abstract AttributesResponse build();
  }
}
