package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypesResponse.class)
public abstract class EntityTypesResponse {
  public abstract LinksResponse getLinks();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract List<EntityTypeResponse> getItems();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract PageResponse getPage();

  public static EntityTypesResponse create(LinksResponse newLinks) {
    return create(newLinks, null, null);
  }

  public static EntityTypesResponse create(
      LinksResponse newLinks, List<EntityTypeResponse> newItems, PageResponse newPage) {
    return builder().setLinks(newLinks).setItems(newItems).setPage(newPage).build();
  }

  public static Builder builder() {
    return new AutoValue_EntityTypesResponse.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setItems(List<EntityTypeResponse> newItems);

    public abstract Builder setPage(PageResponse newPage);

    public abstract EntityTypesResponse build();
  }
}
