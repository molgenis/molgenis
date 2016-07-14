package org.molgenis.data;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.EntityMetaData;

/**
 * Value object to store Entity name / Entity ID combinations.
 */
@AutoValue
public abstract class EntityKey
{
	public abstract String getEntityName();

	public abstract Object getId();

	public static EntityKey create(String entityName, Object id)
	{
		return new AutoValue_EntityKey(entityName, id);
	}

	public static EntityKey create(EntityMetaData entityMetaData, Object id)
	{
		return create(entityMetaData.getName(), id);
	}

	public static EntityKey create(Entity entity)
	{
		return create(entity.getEntityMetaData(), entity.getIdValue());
	}
}
