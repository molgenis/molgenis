package org.molgenis.test.data.staticentity.bidirectional;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class Person extends StaticEntity
{
	public Person(Entity entity)
	{
		super(entity);
	}

	public Person(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public Person(String name, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(name);
	}

	public Person setId(String id)
	{
		set(AuthorMetaData.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(AuthorMetaData.ID);
	}
}
