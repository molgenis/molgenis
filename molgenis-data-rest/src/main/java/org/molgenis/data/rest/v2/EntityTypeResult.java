package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeResult.class)
public abstract class EntityTypeResult
{
	public abstract EntityType getEntityType();

	public abstract boolean isMetadataMatch();

	public abstract List<Attribute> getAttributes();

	public abstract List<Entity> getEntities();

	public static EntityTypeResult create(EntityType entityType, boolean isMetadataMatch, List<Attribute> attributes,
			List<Entity> entities)
	{
		return new AutoValue_EntityTypeResult(entityType, isMetadataMatch, attributes, entities);
	}
}
