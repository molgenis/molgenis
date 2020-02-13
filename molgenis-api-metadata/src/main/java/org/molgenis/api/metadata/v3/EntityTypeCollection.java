package org.molgenis.api.metadata.v3;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.meta.model.EntityType;

@AutoValue
abstract class EntityTypeCollection {

  abstract List<EntityType> getEntityTypes();

  abstract @Nullable @CheckForNull Page getPage();

  /** @return entity id for embedded entity collections, otherwise null */
  abstract @Nullable @CheckForNull String getEntityId();

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setEntityTypes(List<EntityType> newEntities);

    abstract Builder setPage(Page newPage);

    abstract Builder setEntityId(String newEntityId);

    abstract EntityTypeCollection build();
  }

  @SuppressWarnings("unused")
  static EntityTypeCollection create(
      List<EntityType> newEntities, @Nullable @CheckForNull Page newPage, String newEntityId) {
    return builder().setEntityTypes(newEntities).setPage(newPage).setEntityId(newEntityId).build();
  }

  static Builder builder() {
    return new AutoValue_EntityTypeCollection.Builder();
  }

  @AutoValue
  abstract static class Page {

    abstract int getOffset();

    abstract int getTotal();

    abstract int getPageSize();

    @SuppressWarnings("unused")
    static Page create(int newOffset, int newTotal, int newPageSize) {
      return builder().setOffset(newOffset).setTotal(newTotal).setPageSize(newPageSize).build();
    }

    static Builder builder() {
      return new AutoValue_EntityTypeCollection_Page.Builder();
    }

    @SuppressWarnings(
        "java:S1610") // Abstract classes without fields should be converted to interfaces
    @AutoValue.Builder
    abstract static class Builder {

      abstract Builder setOffset(int newOffset);

      abstract Builder setTotal(int newTotal);

      abstract Builder setPageSize(int newPageSize);

      abstract Page build();
    }
  }
}
