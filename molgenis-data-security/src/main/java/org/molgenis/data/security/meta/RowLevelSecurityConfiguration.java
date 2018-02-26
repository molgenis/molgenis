package org.molgenis.data.security.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.security.meta.RowLevelSecuredMetadata.ENTITYTYPE_ID;
import static org.molgenis.data.security.meta.RowLevelSecuredMetadata.ROW_LEVEL_SECURED;

public class RowLevelSecurityConfiguration extends StaticEntity
{
	public RowLevelSecurityConfiguration(Entity entity)
	{
		super(entity);
	}

	public RowLevelSecurityConfiguration(EntityType entityType)
	{
		super(entityType);
	}

	public RowLevelSecurityConfiguration(String id, EntityType entityType)
	{
		super(entityType);
		setEntityTypeId(id);
	}

	public void setRowLevelSecured(Boolean rowLevelSecured)
	{
		set(ROW_LEVEL_SECURED, rowLevelSecured);
	}

	public Boolean isRowLevelSecured()
	{
		return getBoolean(ROW_LEVEL_SECURED);
	}

	public void setEntityTypeId(String entityTypeId)
	{
		set(ENTITYTYPE_ID, entityTypeId);
	}

	public String getEntityTypeId()
	{
		return getString(ENTITYTYPE_ID);
	}
}
