package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityCollectionDeleteRequestV2.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EntityCollectionDeleteRequestV2 {
  @NotEmpty(message = "Please provide at least one entity in the entityIds property.")
  @Size(
      max = RestControllerV2.MAX_ENTITIES,
      message = "Number of entity identifiers cannot be more than {max}.")
  public abstract List<String> getEntityIds();
}
