package org.molgenis.data.rest.v2;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityCollectionBatchRequestV2.class)
public abstract class EntityCollectionBatchRequestV2
{
	@Size(min = 1, max = RestControllerV2.MAX_ENTITIES)
	public abstract List<Map<String, Object>> getEntities();
}