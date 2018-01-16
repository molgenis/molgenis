package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.core.gson.AutoGson;

import javax.validation.constraints.Size;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityCollectionDeleteRequestV2.class)
public abstract class EntityCollectionDeleteRequestV2
{
	@NotEmpty(message = "Please provide at least one entity in the entityIds property.")
	@Size(max = RestControllerV2.MAX_ENTITIES, message = "Number of entity identifiers cannot be more than {max}.")
	public abstract List<String> getEntityIds();
}