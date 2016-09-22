package org.molgenis.test.data.staticentity.bidirectional;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.test.data.staticentity.bidirectional.test1.AuthorMetaData1;

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
		set(AuthorMetaData1.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(AuthorMetaData1.ID);
	}
}
