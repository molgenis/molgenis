package org.molgenis.api.data.v3;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;

@AutoValue
abstract class EntityCollection {
  abstract String getEntityTypeId();

  abstract List<Entity> getEntities();

  abstract @Nullable @CheckForNull Page getPage();

  /** @return entity id for embedded entity collections, otherwise null */
  abstract @Nullable @CheckForNull String getEntityId();

  int getSize() {
    return getEntities().size();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setEntityTypeId(String newEntityTypeId);

    abstract Builder setEntities(List<Entity> newEntities);

    abstract Builder setPage(Page newPage);

    abstract Builder setEntityId(String newEntityId);

    abstract EntityCollection build();
  }

  static EntityCollection create(
      String newEntityTypeId,
      List<Entity> newEntities,
      @Nullable @CheckForNull Page newPage,
      String newEntityId) {
    return builder()
        .setEntityTypeId(newEntityTypeId)
        .setEntities(newEntities)
        .setPage(newPage)
        .setEntityId(newEntityId)
        .build();
  }

  static Builder builder() {
    return new AutoValue_EntityCollection.Builder();
  }

  @AutoValue
  abstract static class Page {

    abstract int getOffset();

    abstract int getTotal();

    abstract int getPageSize();

    static Page create(int newOffset, int newTotal, int newPageSize) {
      return builder().setOffset(newOffset).setTotal(newTotal).setPageSize(newPageSize).build();
    }

    static Builder builder() {
      return new AutoValue_EntityCollection_Page.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {

      abstract Builder setOffset(int newOffset);

      abstract Builder setTotal(int newTotal);

      abstract Builder setPageSize(int newPageSize);

      abstract Page build();
    }
  }
}
