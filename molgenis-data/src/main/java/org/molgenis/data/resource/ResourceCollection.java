package org.molgenis.data.resource;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.util.AutoGson;

/** Collection of typed resources. */
@AutoValue
@AutoGson(autoValueClass = AutoValue_ResourceCollection.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ResourceCollection {

  @NotNull
  public abstract List<Package> getPackages();

  @NotNull
  public abstract List<EntityType> getEntityTypes();

  public static ResourceCollection of(List<Package> packages, List<EntityType> entityTypes) {
    return new AutoValue_ResourceCollection(packages, entityTypes);
  }
}
