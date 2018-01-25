package org.molgenis.data.security.owned;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.data.security.owned.OwnedEntityType.OWNER_USERNAME;

/**
 * Base class for owned entities.
 */
public abstract class OwnedEntity extends StaticEntity
{
	public OwnedEntity(Entity entity)
	{
		super(entity);
	}

	public OwnedEntity(EntityType entityType)
	{
		super(entityType);
	}

	public OwnedEntity(Object id, EntityType entityType)
	{
		super(id, entityType);
	}

	@Nullable
	public String getOwnerUsername()
	{
		return getString(OWNER_USERNAME);
	}

	public void setOwnerUsername(String ownerUsername)
	{
		set(OWNER_USERNAME, ownerUsername);
	}
}
