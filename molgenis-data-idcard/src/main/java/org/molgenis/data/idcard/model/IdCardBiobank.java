package org.molgenis.data.idcard.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.idcard.model.IdCardBiobankMetaData.ORGANIZATION_ID;

public class IdCardBiobank extends StaticEntity
{
	public IdCardBiobank(Entity entity)
	{
		super(entity);
	}

	public IdCardBiobank(EntityType entityType)
	{
		super(entityType);
	}

	public IdCardBiobank(Integer identifier, EntityType entityType)
	{
		super(entityType);
		set(ORGANIZATION_ID, identifier);
	}

	// FIXME add and use getters/setters
}
