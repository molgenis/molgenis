package org.molgenis.data.staticentity.bidirectional;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.staticentity.bidirectional.authorbook1.BookMetaData1;
import org.molgenis.data.support.StaticEntity;

public class Book extends StaticEntity
{
	public Book(Entity entity)
	{
		super(entity);
	}

	public Book(EntityType entityType)
	{
		super(entityType);
	}

	public Book(String name, EntityType entityType)
	{
		super(entityType);
		setId(name);
	}

	public Book setId(String id)
	{
		set(BookMetaData1.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(BookMetaData1.ID);
	}
}
