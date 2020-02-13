package org.molgenis.api.metadata.v3;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.data.meta.model.EntityType;

@AutoValue
abstract class EntityTypes {
  abstract List<EntityType> getEntityTypes();

  abstract int getTotal();

  public static EntityTypes create(List<EntityType> newEntities, int newTotal) {
    return builder().setEntityTypes(newEntities).setTotal(newTotal).build();
  }

  public static Builder builder() {
    return new AutoValue_EntityTypes.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setEntityTypes(List<EntityType> entityTypes);

    public abstract Builder setTotal(int newTotal);

    public abstract EntityTypes build();
  }
}
