package org.molgenis.data.security;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class AclTest extends StaticEntity
{
	public AclTest(Entity entity)
	{
		super(entity);
	}

	public AclTest(EntityType entityType)
	{
		super(entityType);
	}

	public AclTest(Long id, EntityType entityType)
	{
		super(id, entityType);
	}
}