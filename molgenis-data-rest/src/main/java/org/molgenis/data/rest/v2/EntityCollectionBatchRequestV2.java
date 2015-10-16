package org.molgenis.data.rest.v2;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityCollectionBatchRequestV2.class)
public abstract class EntityCollectionBatchRequestV2
{
	@NotEmpty(message = "Please provide at least one entity in the entities property.")
	@Size(min = 1, max = RestControllerV2.MAX_ENTITIES, message = "Number of entities must be between {min} and {max}.")
	public abstract List<Map<String, Object>> getEntities();
}