package org.molgenis.test.data.staticentity.bidirectional;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.test.data.staticentity.bidirectional.test1.BookMetaData1;

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
		set(BookMetaData1.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(BookMetaData1.ID);
	}
}
