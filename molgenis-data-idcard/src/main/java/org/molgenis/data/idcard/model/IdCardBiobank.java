package org.molgenis.data.idcard.model;

import static org.molgenis.data.idcard.model.IdCardBiobankMetaData.ORGANIZATION_ID;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class IdCardBiobank extends StaticEntity
{
	public IdCardBiobank(Entity entity)
	{
		super(entity);
	}

	public IdCardBiobank(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public IdCardBiobank(Integer identifier, EntityMetaData entityMeta)
	{
		super(entityMeta);
		set(ORGANIZATION_ID, identifier);
	}

	// FIXME add and use getters/setters
}
