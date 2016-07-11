package org.molgenis.security.owned;

import static org.molgenis.security.owned.OwnedEntityMetaData.OWNER_USERNAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

/**
 * Base class for owned entities.
 */
public abstract class OwnedEntity extends StaticEntity
{
	public OwnedEntity(Entity entity)
	{
		super(entity);
	}

	public OwnedEntity(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public OwnedEntity(Object id, EntityMetaData entityMeta)
	{
		super(id, entityMeta);
	}

	public String getOwnerUsername()
	{
		return getString(OWNER_USERNAME);
	}

	public void setOwnerUsername(String ownerUsername)
	{
		set(OWNER_USERNAME, ownerUsername);
	}
}
