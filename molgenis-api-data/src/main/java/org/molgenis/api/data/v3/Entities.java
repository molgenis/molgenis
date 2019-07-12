package org.molgenis.api.data.v3;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.data.Entity;

@AutoValue
abstract class Entities {
  abstract List<Entity> getEntities();

  abstract int getTotal();

  public static Entities create(List<Entity> newEntities, int newTotal) {
    return builder().setEntities(newEntities).setTotal(newTotal).build();
  }

  public static Builder builder() {
    return new AutoValue_Entities.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setEntities(List<Entity> newEntities);

    public abstract Builder setTotal(int newTotal);

    public abstract Entities build();
  }
}
