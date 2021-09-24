package org.molgenis.api.data.v3.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntitiesResponse.class)
public abstract class EntitiesResponse {
  public abstract LinksResponse getLinks();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract List<EntityResponse> getItems();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract PageResponse getPage();

  public static EntitiesResponse create(LinksResponse newLinks) {
    return create(newLinks, null, null);
  }

  public static EntitiesResponse create(
      LinksResponse newLinks, List<EntityResponse> newItems, PageResponse newPage) {
    return builder().setLinks(newLinks).setItems(newItems).setPage(newPage).build();
  }

  public static Builder builder() {
    return new AutoValue_EntitiesResponse.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setItems(List<EntityResponse> newItems);

    public abstract Builder setPage(PageResponse newPage);

    public abstract EntitiesResponse build();
  }
}
