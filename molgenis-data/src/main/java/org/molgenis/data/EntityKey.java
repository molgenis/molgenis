package org.molgenis.data;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.EntityType;

/**
 * Value object to store Entity name / Entity ID combinations.
 */
@AutoValue
public abstract class EntityKey
{
	public abstract String getEntityTypeId();

	public abstract Object getId();

	public static EntityKey create(String entityId, Object id)
	{
		return new AutoValue_EntityKey(entityId, id);
	}

	public static EntityKey create(EntityType entityType, Object id)
	{
		return create(entityType.getId(), id);
	}

	public static EntityKey create(Entity entity)
	{
		return create(entity.getEntityType(), entity.getIdValue());
	}
}
