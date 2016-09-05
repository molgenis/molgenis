package org.molgenis.app;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class Book extends StaticEntity
{
	public Book(Entity entity)
	{
		super(entity);
	}

	public Book(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public Book(String name, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(name);
	}

	public Book setId(String id)
	{
		set(BookMetaData.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(BookMetaData.ID);
	}
}
