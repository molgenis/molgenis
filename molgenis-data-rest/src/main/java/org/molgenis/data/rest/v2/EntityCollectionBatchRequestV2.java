package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityCollectionBatchRequestV2.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EntityCollectionBatchRequestV2 {
  @Nullable // See #3897. If hashCode fails, the validation throws an exception
  @NotEmpty(message = "Please provide at least one entity in the entities property.")
  @Size(
      max = RestControllerV2.MAX_ENTITIES,
      message = "Number of entities cannot be more than {max}.")
  public abstract List<Map<String, Object>> getEntities();
}
