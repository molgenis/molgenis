package org.molgenis.security.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.security.meta.ActionPermissionMappingMetadata.ID;

public class ActionPermissionMapping extends StaticEntity
{
	public ActionPermissionMapping(Entity entity)
	{
		super(entity);
	}

	public ActionPermissionMapping(EntityType entityType)
	{
		super(entityType);
	}

	public ActionPermissionMapping(String id, EntityType entityType)
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
}
