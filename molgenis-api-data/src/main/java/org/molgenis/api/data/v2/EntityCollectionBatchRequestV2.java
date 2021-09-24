package org.molgenis.api.data.v2;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityCollectionBatchRequestV2.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EntityCollectionBatchRequestV2 {
  @NotEmpty(message = "Please provide at least one entity in the entities property.")
  @Size(
      max = RestControllerV2.MAX_ENTITIES,
      message = "Number of entities cannot be more than {max}.")
  public abstract List<Map<String, Object>> getEntities();
}
