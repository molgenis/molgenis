package org.molgenis.data.security.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.security.meta.RowLevelSecuredMetadata.*;

public class RowLevelSecured extends StaticEntity
{
	public RowLevelSecured(Entity entity)
	{
		super(entity);
	}

	public RowLevelSecured(EntityType entityType)
	{
		super(entityType);
	}

	public RowLevelSecured(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setRowLevelSecured(Boolean rowLevelSecured)
	{
		set(ROW_LEVEL_SECURED, rowLevelSecured);
	}

	public Boolean isRowLevelSecured()
	{
		return getBoolean(ROW_LEVEL_SECURED);
	}

	public void setEntity(EntityType entityType)
	{
		set(ENTITYTYPE_ID, entityType);
	}

	public EntityType getEntity()
	{
		return getEntity(ENTITYTYPE_ID, EntityType.class);
	}
}
